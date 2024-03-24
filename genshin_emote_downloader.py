#!/usr/bin/env python
import concurrent.futures
import os
import sys
import requests
import urllib.parse

from bs4 import BeautifulSoup, Tag

CONCURRENT_DOWNLOADS = 4

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
DOWNLOADS_DIR = os.path.join(SCRIPT_DIR, "downloads")
RAW_IMAGES_DIR = os.path.join(DOWNLOADS_DIR, "raw")
WHATSAPP_EMOTES_DIR = os.path.join(DOWNLOADS_DIR, "whatsapp")

# TODO: Temporary filepaths to local assets - won't be necessary when real Wiki is used
RAIDEN_EMOTE_LOCAL = os.path.join(
    SCRIPT_DIR, "assets", "Icon_Emoji_Paimon's_Paintings_29_Raiden_Shogun_2.png"
)

URL_CHAT_GALLERY = "https://genshin-impact.fandom.com/wiki/Chat/Gallery#Emojis"


class Emote:
    """Holds the information for a chat emote on the Fandom Wiki."""

    key: str
    set_id: int
    download_url: str
    filename: str

    def __init__(self, key: str, set_id: int, download_url: str) -> None:
        self.key = key
        self.set_id = set_id
        self.download_url = download_url
        self.filename = urllib.parse.unquote(self.key)

    def __repr__(self) -> str:
        return f"Emote [key='{self.key}', set_id={self.set_id}, download_url='{self.download_url}', filename='{self.filename}']"

    def filepath(self, base_dir: str) -> str:
        return os.path.join(base_dir, str(self.set_id), self.filename)


class EmoteSet:
    """Represents a collection of chat emotes."""

    id: int
    preview_emote_key: str | None
    emotes: dict[str, Emote]

    def __init__(self, id: int, preview_emote_key: str | None = None) -> None:
        self.id = id
        self.preview_emote_key = preview_emote_key
        self.emotes = {}

    def __repr__(self) -> str:
        return f"EmoteSet [id={self.id}, preview_emote_key='{self.preview_emote_key}', emotes={self.emotes}]"

    def folderpath(self, base_dir: str) -> str:
        return os.path.join(base_dir, str(self.id))


def retrieve_gallery_page() -> BeautifulSoup | None:
    """Retrieves the HTML contents of the Chat/Gallery page on the Fandom Wiki as a `BeautifulSoup` instance."""

    gallery_request = requests.get(URL_CHAT_GALLERY)
    return BeautifulSoup(gallery_request.content, features="html5lib")


def is_set_span_tag(tag: Tag) -> bool:
    """Returns whether this `Tag` represents a 'Set N' `<span>` heading on the Chat/Gallery page."""

    return (
        tag.name == "span"
        and "mw-headline" in tag.get("class", "")
        and tag.get("id", "").startswith("Set_")
    )


def is_emote_img_tag(tag: Tag) -> bool:
    """Returns whether this `Tag` represents an emote `<img>` tag on the Chat/Gallery page."""

    return (
        tag.name == "img"
        and tag.get("data-src", "").startswith("https")
        and tag.get("data-image-key", "").startswith("Icon_Emoji_Paimon%27s_Paintings_")
    )


def is_emote_preview_img_tag(tag: Tag) -> bool:
    """Returns whether this `Tag` represents an emote `<img>` tag for a set's preview on the Chat/Gallery page."""

    return is_emote_img_tag(tag) and not tag.has_attr("data-caption")


def is_emote_gallery_img_tag(tag: Tag) -> bool:
    """Returns whether this `Tag` represents an emote `<img>` tag for a gallery image on the Chat/Gallery page."""

    return is_emote_img_tag(tag) and tag.has_attr("data-caption")


def original_quality_url(downscaled_url: str) -> str:
    """Gets a URL that points to the original quality image, based on a URL to a downscaled image."""

    base_url = downscaled_url.split("/revision", 1)[0]
    return f"{base_url}/revision/latest?format=original"


def download_emote(emote: Emote, replace: bool = False):
    """
    Downloads the original quality image for an emote into the downloads folder.
    The image is only downloaded if it isn't already present or the `replace` parameter is `True`.
    """

    img_filepath = emote.filepath(RAW_IMAGES_DIR)
    if not replace and os.path.exists(img_filepath):
        print(f"Skipping '{emote.filename}'; already downloaded")
        return

    try:
        print(f"Downloading '{emote.filename}'...")

        with open(img_filepath, "wb") as img_file:
            img_request = requests.get(emote.download_url)
            img_file.write(img_request.content)

        print(f"Downloaded '{emote.filename}'")
    except Exception as e:
        print(f"Unable to download {emote.filename}:", e)

        # Try to delete any partially downloaded contents, so we can try again later
        try:
            os.remove(img_filepath)
        except Exception:
            pass


def query_emote_sets(html: BeautifulSoup) -> dict[int, EmoteSet]:
    """Queries the emote sets from the Chat/Gallery page HTML."""

    # Query which emote sets are present
    # On the gallery page, there are various "Set N" headings, represented by a <span> tag:
    # <span id="Set_X" class="mw-headline">
    #
    # It also has an inner <img> tag - the 'data-image-key' attribute determines which one should be used for the preview
    emote_sets: dict[int, EmoteSet] = {}

    set_span_tag: Tag
    for set_span_tag in html.find_all(is_set_span_tag):
        set_id = int(set_span_tag.get("id").removeprefix("Set_"))

        if set_id not in emote_sets:
            preview_img_tag = set_span_tag.find(is_emote_preview_img_tag)
            preview_emote_key = preview_img_tag.get("data-image-key")
            emote_sets[set_id] = EmoteSet(set_id, preview_emote_key)

    # Query which emotes are present and add them to a set
    # On the gallery page, the relevant information can be found in <img> tags, where the 'data-image-key' attribute has this prefix:
    # Icon_Emoji_Paimon%27s_Paintings_
    #
    # The Set ID can be derived from the image key (the first number after 'Paintings_')
    # The downscaled URL can be retrieved from the 'data-src' attribute
    # We can convert the URL to retrieve the original quality image by using '&format=original'
    emote_img_tag: Tag
    for emote_img_tag in html.find_all(is_emote_gallery_img_tag):
        emote_key = emote_img_tag.get("data-image-key")
        set_id = int(
            emote_key.removeprefix("Icon_Emoji_Paimon%27s_Paintings_").split("_", 1)[0]
        )
        emote_set = emote_sets[set_id]

        if emote_key not in emote_set.emotes:
            download_url = original_quality_url(emote_img_tag.get("data-src"))
            emote_set.emotes[emote_key] = Emote(emote_key, set_id, download_url)

    return emote_sets


def main():
    replace = "--replace" in sys.argv

    html = retrieve_gallery_page()
    emote_sets = query_emote_sets(html)

    print(f"Found {len(emote_sets)} emote sets")

    with concurrent.futures.ThreadPoolExecutor(CONCURRENT_DOWNLOADS) as executor:
        for emote_set in emote_sets.values():
            os.makedirs(emote_set.folderpath(RAW_IMAGES_DIR), exist_ok=True)
            os.makedirs(emote_set.folderpath(WHATSAPP_EMOTES_DIR), exist_ok=True)

            for emote in emote_set.emotes.values():
                executor.submit(download_emote, emote, replace)


if __name__ == "__main__":
    main()
