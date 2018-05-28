#ifndef SERVER_USER_H
#define SERVER_USER_H

#include <openssl/rand.h>

#include "main.h"
#include "Database.h"

using bsoncxx::oid;
using std::vector;

class UserManager;

struct File {
    string filename;
    string hash;
    uint64_t size;
    uint64_t creation_date;
    bool isValid;
    uint64_t lastValid;
    oid owner;
};

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
    User(UserManager&);
    bool getName(string&);
    bool getHomeDir(string&);
    bool getSurname(string&);
    bool setName(string&);
    bool setPassword(string&);
    bool loginByPassword(string&, string&);
    bool loginBySid(string&);
    bool logout(string&);
    bool addUsername(const string&);
    bool isValid() { return valid; };
    bool isAuthorized() { return authorized; };
    bool listFilesinPath(string&, vector<string>&);
};

class UserManager {
private:
    Database& db;

    explicit UserManager(Database&);
    string mapToString(std::map<string, bsoncxx::document::element>&);
public:
    bool getName(oid&, string&);
    bool getSurname(oid&, string&);
    bool getHomeDir(oid&, string&);
    bool getPasswdHash(oid&, string&);
    bool setPasswdHash(oid&, string&);
    bool setName(oid&, string&);
    bool addSid(oid&, string&);
    bool checkSid(oid&, string&);
    bool getUserId(const string& username, oid& id);
    bool removeSid(oid&, string&);
    bool listAllUsers(std::vector<string>&);

    bool listFilesinPath(oid&, string&, vector<string>&);

    static UserManager& getInstance(Database* db = nullptr)
    {
        static UserManager instance(*db);
        return instance;
    }
};

#endif //SERVER_USER_H
