import flickrapi
import numpy as np
import re
import bisect






file_str = '/media/datasets/flickr/yfcc100m_dataset'
i = 0
key = 'bfebdc12c8a54772e3a7582668ee636c'
secret = '526e1000330cb7d3'
flickr = flickrapi.FlickrAPI(key, secret)
tag_counts = dict()
num_most_recurrent = 200
for line in open(file_str):

    url_str = line.split('\t')
    tags_str = url_str[10]
    #print i, tags, '****', url_str[11]
    tags = tags_str.split(',')
    for tag in tags:
        if(tag==''):
            continue
        current_count = tag_counts.get(tag, 0) + 1
        if(current_count >1 or len(tag_counts.keys()) < num_most_recurrent):
            tag_counts[tag] = current_count
        else:
            min_val_index = np.argmin(tag_counts.values())
            min_val = tag_counts.values()[min_val_index]
            min_tag = tag_counts.keys()[min_val_index]
            if(current_count > min_val):
                tag_counts[tag] = current_count
                tag_counts.pop(min_tag, None)
    if(i%100000==0):
        print tag_counts, len(tag_counts.keys()), '\n', '*****'
    i += 1
print tags