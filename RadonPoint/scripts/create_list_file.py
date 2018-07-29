import numpy as np
import os
list_file_name = '/media/datasets/flickr/list/flickr_list_id.txt'

output_file = open(list_file_name, 'w+')
im_path = '/media/datasets/flickr/images/'
file_names = os.listdir(im_path)


for file_name in file_names:
    full_file_name = im_path + file_name
    output_file.write(file_name+'\n')
    