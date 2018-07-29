
import numpy as np








file_str = '/media/datasets/flickr/CNN_features/XXX_ab.fastq.gz'
data_file = 'YFCC100M_hybridCNN_gmean_fc6_0.txt.gz'
i = 0

import magic

blob = open(file_str).read()
m = magic.Magic(mime_encoding=True)
encoding = m.from_buffer(blob)
print encoding 

for line in open(file_str):
    print line

    url_str = line.split('\t')
