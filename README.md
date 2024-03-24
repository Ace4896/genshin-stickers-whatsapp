# Genshin Chat Emotes Downloader

A small script which downloads Genshin Chat Emotes from the [Fandom Wiki](https://genshin-impact.fandom.com/wiki/Chat/Gallery#Emojis).

## Requirements

- Python 3.12

## Usage

First, setup a virtual environment:

- Create the virtual environment: `python -m venv .venv`
- Activate it:
  - Bash: `source ./.venv/bin/activate`
  - PowerShell: `.\.venv\bin/Activate.ps1`
- Install the dependencies into the virtual environment: `pip install -r requirements.txt`

The script can be used as follows:

```bash
python3 ./genshin_emote_downloader.py [--refresh]
```

Arguments:

- `--refresh`: Refreshes all locally downloaded images

## License

The code in this repository is licensed under [MIT](./LICENSE).

The chat emotes downloaded by this script are copyrighted to HoYoverse. This is a fan project which has no affiliation with HoYoverse.
