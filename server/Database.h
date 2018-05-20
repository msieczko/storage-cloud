#ifndef SERVER_DATABASE_H
#define SERVER_DATABASE_H

#include "main.h"
#include "Logger.h"

#include <mongocxx/instance.hpp>
#include <mongocxx/uri.hpp>
#include <mongocxx/client.hpp>

#include <bsoncxx/array/view.hpp>
#include <bsoncxx/array/view.hpp>
#include <bsoncxx/builder/basic/array.hpp>
#include <bsoncxx/builder/basic/document.hpp>
#include <bsoncxx/builder/basic/kvp.hpp>
#include <bsoncxx/document/value.hpp>
#include <bsoncxx/document/view.hpp>
#include <bsoncxx/json.hpp>
#include <bsoncxx/stdx/string_view.hpp>
#include <bsoncxx/string/to_string.hpp>
#include <bsoncxx/types.hpp>
#include <bsoncxx/types/value.hpp>

#define DB_URI "mongodb://localhost:27017"
#define DB_NAME "tin"

using std::string;

struct User {
    string username;
    string password;
    string name;
    string surname;
    string home_dir;
    string print() {
        string s = "Username: " + username;
        s += " password: " + password;
        s += " name: " + name + " " + surname;
        return s;
    }
};

class Database {
private:
    mongocxx::client* client;
    mongocxx::database db;
    Logger* logger;
    std::string id = "db";

public:
    Database(Logger*);
    bool listUsers();
};

#endif //SERVER_DATABASE_H
