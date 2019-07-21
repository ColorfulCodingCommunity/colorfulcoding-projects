import numpy as np
import cv2
from matplotlib import pyplot as plt

img1 = cv2.imread('assets/augmented-images-earth.jpg')
cv2.resize(img1, (512, 512))
img1 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
img1 = cv2.blur(img1, (5, 5))

orb = cv2.ORB_create()
kp1, des1 = orb.detectAndCompute(img1, None)

cap = cv2.VideoCapture(0)
while(cap.isOpened()):
    ret, frame = cap.read()
    gray = cv2.resize(frame, (512, 512))
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    gray = blur = cv2.blur(gray, (5, 5))

    kp2, des2 = orb.detectAndCompute(gray, None)

    bruteForceMatcher = cv2.BFMatcher(cv2.NORM_HAMMING, crossCheck=True)
    matches = bruteForceMatcher.match(des1, des2)
    matches = sorted(matches, key=lambda x: x.distance)

    srcPoints = np.float32(
        [kp1[m.queryIdx].pt for m in matches]).reshape(-1, 1, 2)
    dstPoints = np.float32(
        [kp2[m.trainIdx].pt for m in matches]).reshape(-1, 1, 2)

    M, mask = cv2.findHomography(srcPoints, dstPoints, cv2.RANSAC, 3.0)
    
    matchesMask = mask.ravel().tolist()

    h, w = img1.shape
    
    pts = np.float32([[0, 0], [0, h-1], [w-1, h-1], [w-1, 0]]
                     ).reshape(-1, 1, 2)
    dst = cv2.perspectiveTransform(pts, M)

    gray = cv2.polylines(gray, [np.int32(dst)], True, 255, 3, cv2.LINE_AA)

    cv2.imshow('frame', gray)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break


cap.release()
cv2.destroyAllWindows()