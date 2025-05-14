from pdf2image import convert_from_path
import os

pdf_path = 'hanzi.pdf'  # Replace with your PDF
output_folder = 'worksheets'
dpi = 200  # Lower DPI to save memory
fmt = 'png'

os.makedirs(output_folder, exist_ok=True)

# Use batch size to prevent memory blowup
pages = convert_from_path(
    pdf_path,
    dpi=dpi,
    fmt=fmt,
    output_folder=output_folder,
    paths_only=True,
    thread_count=2
)

# Rename files for clarity
for i, path in enumerate(sorted(pages)):
    new_path = os.path.join(output_folder, f'page_{i+1}.{fmt}')
    os.rename(path, new_path)
    print(f'Saved: {new_path}')
