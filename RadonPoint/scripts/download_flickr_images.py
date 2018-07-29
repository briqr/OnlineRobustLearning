import flickrapi
import numpy as numpy
import re
import urllib

import os.path




file_str = '/media/datasets/flickr/yfcc100m_dataset'
i = 0
key = 'your_key'
secret = 'your_secret'
flickr = flickrapi.FlickrAPI(key, secret)

for line in open(file_str):


    url_str = re.split(r'\t+', line)
    photo_id1 = int(url_str[1])
    if(os.path.exists('/media/datasets/flickr/images/'+str(photo_id1)+'.jpg')):
        continue
   
    print '****', photo_id1
    try:
        a = flickr.photos_getSizes(api_key=key, photo_id=photo_id1)
    except flickrapi.exceptions.FlickrError:
        continue
    l = 0
    for x in a.iter():
        if(l==6):
            source = x.get('source')
            urllib.urlretrieve(source, '/media/datasets/flickr/images/'+str(photo_id1)+'.jpg')
            print 'retrieved'
        l += 1
        

    i += 1

    





