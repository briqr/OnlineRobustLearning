import flickrapi
import numpy as numpy
import re
import urllib

import os.path




file_str = '/media/datasets/flickr/yfcc100m_dataset'
i = 0
key = 'bfebdc12c8a54772e3a7582668ee636c'
secret = '526e1000330cb7d3'
flickr = flickrapi.FlickrAPI(key, secret)

for line in open(file_str):


    url_str = re.split(r'\t+', line)
    photo_id1 = int(url_str[1])
    if(os.path.exists('/media/datasets/flickr/images/'+str(photo_id1)+'.jpg')):
        print 'exists'
        continue
    
    #url_str = url_str[len(url_str)-3]
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
        
    #print '---', a.getchildren() #a.get('stat')[0]
    #if(i==5):
    #    break
    i += 1
    #flickr.photos_getSizes(k_id = id)
    





