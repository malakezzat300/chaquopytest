import base64
import io

import cv2
import math
import numpy as np
from PIL import Image

global img
global EncryptionImg
global DecryptionImg

def int2bin8(x):  # Integer to 8-bit binary
    result = "";
    for i in range(8):
        y = x & (1)
        result += str(y)
        x = x >> 1
    return result[::-1]


def int2bin16(x):  # Integer to 8-bit binary
    result = "";
    for i in range(16):
        y = x & (1)
        result += str(y)
        x = x >> 1
    return result


def Encryption(img, j0, g0, x0, EncryptionImg):
    x = img.shape[0]
    y = img.shape[1]
    c = img.shape[2]
    g0 = int2bin16(g0)
    for s in range(x):
        for n in range(y):
            for z in range(c):
                m = int2bin8(img[s][n][z])  # Pixel value to octet binary
                ans = ""
                # print("ok")
                for i in range(8):
                    ri = int(g0[-1])  # Take the last digit of the manual cipher machine
                    qi = int(m[i]) ^ ri  # XOR with pixel value qi
                    xi = 1 - math.sqrt(abs(2 * x0 - 1))  # f1(x) chaotic iteration
                    if qi == 0:  # If qi=0, use x0i+x1i=1;
                        xi = 1 - xi;
                    x0 = xi  # xi iteration
                    t = int(g0[0]) ^ int(g0[12]) ^ int(g0[15])  # Primitive polynomial x^15+x^3+1
                    g0 = str(t) + g0[0:-1]  # gi iteration
                    ci = math.floor(xi * (2 ** j0)) % 2  # Nonlinear transformation operator
                    ans += str(ci)
                re = int(ans, 2)
                EncryptionImg[s][n][z] = re  # Write new image


def Decryption(EncryptionImg, j0, g0, x0, DecryptionImg):
    x = EncryptionImg.shape[0]
    y = EncryptionImg.shape[1]
    c = EncryptionImg.shape[2]
    g0 = int2bin16(g0)
    for s in range(x):
        for n in range(y):
            for z in range(c):
                cc = int2bin8(EncryptionImg[s][n][z])
                ans = ""
                # print("no")
                for i in range(8):
                    xi = 1 - math.sqrt(abs(2 * x0 - 1))
                    x0 = xi
                    ssi = math.floor(xi * (2 ** j0)) % 2
                    qi = 1 - (ssi ^ int(cc[i]))
                    ri = int(g0[-1])
                    mi = ri ^ qi
                    t = int(g0[0]) ^ int(g0[12]) ^ int(g0[15])
                    g0 = str(t) + g0[0:-1]
                    ans += str(mi)
                re = int(ans, 2)
                DecryptionImg[s][n][z] = re


def Encrypt(data):
    # encryption image function its input is the path of image to encrypt
    # the output should be encrypted
    decoded_data = base64.b64decode(data)
    np_data = np.frombuffer(decoded_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    # img = cv2.imread(r"F:\3lom\image project\test_image.png", 1) # Read original image
    EncryptionImg = np.zeros(img.shape, np.uint8)  # make blank image with the same size to store encrypted image
    Encryption(img, 10, 30, 0.123345, EncryptionImg)  # encryption
    pil_im = Image.fromarray(EncryptionImg)
    buff = io.BytesIO()
    pil_im.save(buff, format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    

    decoded_data2 = base64.b64decode(img_str)
    np_data2 = np.frombuffer(decoded_data2, np.uint8)
    img2 = cv2.imdecode(np_data2, cv2.IMREAD_UNCHANGED)

    # img = cv2.imread(r"F:\3lom\image project\test_image.png", 1) # Read original image
    DecryptionImg2 = np.zeros(img2.shape, np.uint8)
    Decryption(img2, 10, 30, 0.123345, DecryptionImg2)  # decrypt
    pil_im2 = Image.fromarray(DecryptionImg2)
    buff2 = io.BytesIO()
    pil_im2.save(buff2, format="PNG")
    img_str2 = base64.b64encode(buff2.getvalue())

    decoded_data3 = base64.b64decode(img_str2)
    np_data3 = np.frombuffer(decoded_data3, np.uint8)
    img3 = cv2.imdecode(np_data3, cv2.IMREAD_UNCHANGED)

    # img = cv2.imread(r"F:\3lom\image project\test_image.png", 1) # Read original image
    EncryptionImg3 = np.zeros(img3.shape, np.uint8)  # make blank image with the same size to store encrypted image
    Encryption(img3, 10, 30, 0.123345, EncryptionImg3)  # encryption
    pil_im3 = Image.fromarray(EncryptionImg3)
    buff3 = io.BytesIO()
    pil_im3.save(buff3, format="PNG")
    img_str3 = base64.b64encode(buff3.getvalue())
    
    return "" + str(img_str3, 'utf-8')

def Decrypt(data):
    # encryption image function its input is the path of image to encrypt
    # the output should be encrypted
    decoded_data = base64.b64decode(data)
    np_data = np.frombuffer(decoded_data, np.uint8)
    img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    # img = cv2.imread(r"F:\3lom\image project\test_image.png", 1) # Read original image
    DecryptionImg = np.zeros(img.shape, np.uint8)
    Decryption(img, 10, 30, 0.123345, DecryptionImg)  # decrypt
    pil_im = Image.fromarray(DecryptionImg)
    buff = io.BytesIO()
    pil_im.save(buff, format="PNG")
    img_str = base64.b64encode(buff.getvalue())
    return "" + str(img_str, 'utf-8')



if __name__ == "__main__":
    img = cv2.imread(r"F:\3lom\image project\test_image.png", 1)  # Read original image
    EncryptionImg = np.zeros(img.shape, np.uint8)
    Encryption(img, 10, 30, 0.123345, EncryptionImg)  # encryption
    cv2.imwrite(r"F:\3lom\image project\Correlation_matrix-EncryptionImg.png",
                EncryptionImg)  # Save the encrypted image

    img = cv2.imread(r"F:\3lom\image project\Correlation_matrix-EncryptionImg.png", 1)  # Read encrypted image
    DecryptionImg = np.zeros(img.shape, np.uint8)
    Decryption(img, 10, 30, 0.123345, DecryptionImg)  # decrypt
    cv2.imwrite(r"F:\3lom\image project\Correlation_matrix-DecryptionImg.png",
                DecryptionImg)  # Save the decrypted image

    cv2.waitKey(0)

