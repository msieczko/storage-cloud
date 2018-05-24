#ifndef SERVER_USER_H
#define SERVER_USER_H

#include "main.h"
#include "Database.h"

using bsoncxx::oid;

class UserManager;

class User {
private:
    oid id;
    UserManager& user_manager;

public:
    User(string&, UserManager&);
    User(oid&, UserManager&);
    bool getName(string&);
    bool getSurname(string&);
    bool checkPassword(string&);
    bool setName(string&);
    bool setPassword(string&);
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

    static UserManager& getInstance(Database* db = nullptr)
    {
        static UserManager instance(*db);

        return instance;
    }

    bool getUserId(string& username, oid& id);
};

#endif //SERVER_USER_H
