import sqlite3
from Cryptodome.Random import get_random_bytes
from Cryptodome.Util.Padding import pad, unpad 
from Cryptodome.Cipher import AES
from Cryptodome.Hash import SHAKE128
from base64 import b64encode, b64decode
from Cryptodome.Protocol.KDF import PBKDF2
from Cryptodome.Hash import SHA3_256,SHA512
from Cryptodome.Random import get_random_bytes

def afficher_contenu_base_de_donnees(passwd,nom_base_de_donnees):
    pwd=bytes(passwd.encode('utf-8'))
    connexion = sqlite3.connect(nom_base_de_donnees)
    curseur = connexion.cursor()
    curseur.execute("SELECT * FROM PASSWORDS")
    resultats = curseur.fetchall()
    for resultat in resultats[1:]:
        nom_organisme = resultat[0]
        chiffre = resultat[1]
        #print(nom_organisme)
        #print(len(chiffre))
        #print(resultat)
        salt = chiffre[16:32]
        ivchiffre = chiffre[:16]
        cryptogramme = chiffre[32:]
        cle = PBKDF2(pwd, salt, 16, count=10000,  hmac_hash_module=None)
        ivnull = bytes([0]*16)
        #aescbciv =  AES.new(cle,AES.MODE_CBC,iv=ivnull)
        aescbciv =  AES.new(cle,AES.MODE_ECB)
        ivclair = aescbciv.decrypt(ivchiffre)
        aescbc =  AES.new(cle,AES.MODE_CBC,iv=ivclair)
        message = aescbc.decrypt(cryptogramme)
        print(nom_organisme)
        print(unpad(message,16).decode('utf-8'))
        print("")
    connexion.close()

nom_de_la_base_de_donnees = "passwords.db"
mdp = input("Entrez mot de passe: ")
afficher_contenu_base_de_donnees(mdp,nom_de_la_base_de_donnees)

