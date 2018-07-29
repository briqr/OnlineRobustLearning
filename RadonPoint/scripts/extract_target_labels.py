
import numpy as np







#feature_file = '/media/datasets/flickr/CNN_features/YFCC100M_hybridCNN_gmean_fc6_10.txt'
#feature_file = '/media/datasets/flickr/alexnet_features/features.txt'
label_file = '/media/datasets/flickr/yfcc100m_dataset'
target_file_name = '/media/datasets/flickr/alexnet_features/target_data_photo_label_vgg19.txt'
img_list_path = '/media/datasets/flickr/list/flickr_list.txt'
target_ids = set()
target_ids_dict = dict()
i = 0
for line in open(img_list_path):
    img_name = line.strip()
    im_id = img_name.split('/')[5].split('.')[0]
    #print url_str

    target_ids.add(im_id)
    print im_id
    #print url_str[0]
    if(i%10000==0):
        print i
    i += 1

i = 0
for line in open(label_file):
    url_str = line.split('\t')
    tags_str = url_str[10].strip()

    photo_id = url_str[1]
    if(photo_id in target_ids):
        target_ids_dict[photo_id] = tags_str
        if(i%10000==0):
            print i, "found"
        i += 1


target_f = open(target_file_name, 'w+')

for line in open(img_list_path):
    img_name = line.strip()
    photo_id = img_name.split('/')[5].split('.')[0]

    print photo_id
    if(not photo_id in target_ids_dict):
        print photo_id, ' not found'
        continue
    tags = target_ids_dict[photo_id] 
    target_f.write(photo_id + ' ' + tags + '\n')