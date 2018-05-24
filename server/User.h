#ifndef SERVER_USER_H
#define SERVER_USER_H

#include <openssl/rand.h>

#include "main.h"
#include "Database.h"

using bsoncxx::oid;

class UserManager;

class User {
private:
    oid id;
    UserManager& user_manager;
    bool authorized;
    bool valid;

    bool checkPassword(string&);

public:
    User(const string&, UserManager&);
    User(oid&, UserManager&);
    bool getName(string&);
    bool getSurname(string&);
    bool setName(string&);
    bool setPassword(string&);
    bool loginByPassword(string&, string&);
    bool loginBySid(string&, string&);
    bool isValid() { return valid; };
    bool isAuthorized() { return authorized; };
};

class UserManager {
private:
    Database& db;

    explicit UserManager(Database&);
public:

    bool getName(oid&, string&);
    bool getSurname(oid&, string&);
    bool getPasswdHash(oid&, string&);
    bool setPasswdHash(oid&, string&);
    bool setName(oid&, string&);
    bool addSid(oid&, string&);

    static UserManager& getInstance(Database* db = nullptr)
    {
        static UserManager instance(*db);

        return instance;
    }

    bool getUserId(const string& username, oid& id);
};

#endif //SERVER_USER_H
