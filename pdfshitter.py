"""

This is a python script which takes in a pdf and then "shits" it out as images into a folder called "worksheets"

"""

from pdf2image import convert_from_path
import os

pdf_path = 'hanzi.pdf'  # Replace with whatever you pdf is called
output_folder = 'worksheets'
dpi = 200  # You might need to change this ??
fmt = 'png'

os.makedirs(output_folder, exist_ok=True)


pages = convert_from_path(
    pdf_path,
    dpi=dpi,
    fmt=fmt,
    output_folder=output_folder,
    paths_only=True,
    thread_count=2
)

for i, path in enumerate(sorted(pages)):
    new_path = os.path.join(output_folder, f'page_{i+1}.{fmt}') # Names the files as "page_xx.png"
    os.rename(path, new_path)
    print(f'Saved: {new_path}')
