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

class User {
private:

    std::vector<std::pair<string*, string> > stringFields;
public:
    string username;
    string password;
    string name;
    string surname;
    string home_dir;
    bsoncxx::oid id{};
    string print() {
        string s = "id: " + id.to_string();
        s += " username: " + username;
        s += " password: " + password;
        s += " name: " + name + " " + surname;
        return s;
    }

    User() {
        stringFields.emplace_back(make_pair(&username, "username"));
        stringFields.emplace_back(make_pair(&password, "password"));
        stringFields.emplace_back(make_pair(&name, "name"));
        stringFields.emplace_back(make_pair(&surname, "surname"));
        stringFields.emplace_back(make_pair(&home_dir, "homeDir"));
    }

//    string getUsername() {
//        Database::
//    }

    bsoncxx::builder::basic::document toBSON() {
        auto doc = bsoncxx::builder::basic::document{};
        using bsoncxx::builder::basic::kvp;

        for(auto field: stringFields) {
            doc.append(kvp(field.second, *(field.first)));
        }

        doc.append(kvp("_id", id));

        return doc;
    }

    void fromBSON(bsoncxx::document::view doc) {
        bool ok = true;

        using namespace std;

        for(auto field: stringFields) {
            auto val = doc[field.second];
            if (!val) {
                ok = false;
            } else {
                *(field.first) = bsoncxx::string::to_string(val.get_utf8().value);
            }
        }

        auto files = doc["files"];
        if(!files || files.type() != bsoncxx::types::b_array::type_id) {
            cout<<"not array"<<endl;
        } else {
            cout<<"files:"<<endl;
            for (auto ele: files.get_array().value) {
                auto val = ele["name"];
                if(!val) {
                    continue;
                }
                cout<<"name: "<<bsoncxx::string::to_string(val.get_utf8().value)<<endl;
            }
        }

        id = doc["_id"].get_oid().value;

        if(ok) {
//            cout<<print()<<endl;
        } else {
            cout<<"wrong user"<<endl;
        }
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
    bool listUsers(std::vector<User>&);
    bool addUser(User&);
    bool getField(string, string, bsoncxx::oid, string&);
};

#endif //SERVER_DATABASE_H
