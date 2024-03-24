#!/usr/bin/env python
import os
import urllib.parse

from bs4 import BeautifulSoup, Tag

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
DOWNLOADS_DIR = os.path.join(SCRIPT_DIR, "downloads")
RAW_IMAGES_DIR = os.path.join(DOWNLOADS_DIR, "raw")
WHATSAPP_EMOTES_DIR = os.path.join(DOWNLOADS_DIR, "whatsapp")

# TODO: Temporary filepaths to local assets - won't be necessary when real Wiki is used
HTML_CHAT_GALLERY_LOCAL = os.path.join(
    SCRIPT_DIR, "assets", "Chat_Gallery Genshin Impact Wiki Fandom.htm"
)
RAIDEN_EMOTE_LOCAL = os.path.join(
    SCRIPT_DIR, "assets", "Icon_Emoji_Paimon's_Paintings_29_Raiden_Shogun_2.png"
)

URL_CHAT_GALLERY = "https://genshin-impact.fandom.com/wiki/Chat/Gallery#Emojis"


class Emote:
    """Holds the information for a chat emote on the Fandom Wiki."""

    key: str
    download_url: str
    filename: str

    def __init__(self, key: str, download_url: str) -> None:
        self.key = key
        self.download_url = download_url
        self.filename = urllib.parse.unquote(self.key)

    def __repr__(self) -> str:
        return f"Emote [key='{self.key}', download_url='{self.download_url}', filename='{self.filename}']"


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


def retrieve_gallery_page() -> BeautifulSoup | None:
    """Retrieves the HTML contents of the Chat/Gallery page on the Fandom Wiki as a `BeautifulSoup` instance."""

    # TODO: When the code is more polished, change this to query the real Fandom Wiki
    with open(HTML_CHAT_GALLERY_LOCAL, "rb") as file:
        return BeautifulSoup(file, features="html5lib")


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


def original_quality_url(downscaled_url: str) -> str:
    """Gets a URL that points to the original quality image, based on a URL to a downscaled image."""

    base_url = downscaled_url.split("/revision", 1)[0]
    return f"{base_url}/revision/latest?format=original"


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
            preview_img_tag = set_span_tag.find(is_emote_img_tag)
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
    for emote_img_tag in html.find_all(is_emote_img_tag):
        emote_key = emote_img_tag.get("data-image-key")
        set_id = int(
            emote_key.removeprefix("Icon_Emoji_Paimon%27s_Paintings_").split("_", 1)[0]
        )
        emote_set = emote_sets[set_id]

        if emote_key not in emote_set.emotes:
            download_url = original_quality_url(emote_img_tag.get("data-src"))
            emote_set.emotes[emote_key] = Emote(emote_key, download_url)

    return emote_sets


def main():
    html = retrieve_gallery_page()
    emote_sets = query_emote_sets(html)

    print(emote_sets[1])


if __name__ == "__main__":
    main()
