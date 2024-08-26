# Downloader for Genshin Impact Chat Emotes

A small script which downloads Genshin Impact's chat emotes from the [Fandom Wiki](https://genshin-impact.fandom.com/wiki/Chat/Gallery#Emojis) and packages into WhatsApp sticker format.

## Requirements

- Python 3.12

## Usage

First, setup a virtual environment:

- Create the virtual environment: `python -m venv .venv`
- Activate it:
  - Bash: `source ./.venv/bin/activate`
  - PowerShell: `.\.venv\Script\Activate.ps1`
- Install the dependencies into the virtual environment: `pip install -r requirements.txt`

The script can be used as follows:

```bash
python3 ./downloader.py [--refresh]
```

Arguments:

- `--refresh`: Refreshes all locally downloaded images

After running the script, the raw and processed images can be found in `downloads`.
