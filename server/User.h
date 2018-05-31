#ifndef SERVER_USER_H
#define SERVER_USER_H

#include <openssl/rand.h>

#include "main.h"
#include "Database.h"

#define FILE_REGULAR 1
#define FILE_DIR 2

#define ADD_FILE_OK 0
#define ADD_FILE_WRONG_DIR 1
#define ADD_FILE_NO_SPACE 2
#define ADD_FILE_INTERNAL_ERROR 3
#define ADD_FILE_FILE_EXISTS 4
#define ADD_FILE_EMPTY_NAME 5

using bsoncxx::oid;
using std::vector;

class UserManager;

// user file
struct UFile {
    string filename;
    string hash;
    uint64_t size;
    uint64_t creation_date;
    bool isValid;
    uint64_t lastValid;
    string owner_name;
    uint8_t type;
};

class User {
private:
    oid id;
    UserManager& user_manager;
    bool authorized;
    bool valid;

    // home dir is pernament
    string home_dir;

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
    bool listFilesinPath(const string&, vector<UFile>&);
    uint8_t addFile(UFile&);
};

class UserManager {
private:
    Database& db;
    string root_path = "/storage";

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
    bool yourFileExists(oid &, const string &);
    bool yourFileIsDir(oid &, const string &);
    bool getUserId(const string& username, oid& id);
    bool removeSid(oid&, string&);
    bool listAllUsers(std::vector<string>&);

    bool listFilesinPath(oid&, const string&, vector<UFile>&);
    bool addFile(oid&, UFile&, string& home_dir);

    static UserManager& getInstance(Database* db = nullptr)
    {
        static UserManager instance(*db);
        return instance;
    }
};

#endif //SERVER_USER_H
