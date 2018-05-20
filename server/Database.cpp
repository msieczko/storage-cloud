//
// Created by milosz on 20.05.18.
//

#include "Database.h"

#include <iostream>

#include <bsoncxx/builder/stream/document.hpp>
#include <bsoncxx/json.hpp>

#include <mongocxx/client.hpp>
#include <mongocxx/instance.hpp>

//int main(int, char**) {
//    mongocxx::instance inst{};
//    mongocxx::client conn{mongocxx::uri{}};
//
//    bsoncxx::builder::stream::document document{};
//
//    auto collection = conn["testdb"]["testcollection"];
//    document << "hello" << "world";
//
//    collection.insert_one(document.view());
//    auto cursor = collection.find({});
//
//    for (auto&& doc : cursor) {
//        std::cout << bsoncxx::to_json(doc) << std::endl;
//    }
//}
using namespace mongocxx;
using namespace std;

using bsoncxx::builder::stream::close_array;
using bsoncxx::builder::stream::close_document;
using bsoncxx::builder::stream::document;
using bsoncxx::builder::stream::finalize;
using bsoncxx::builder::stream::open_array;
using bsoncxx::builder::stream::open_document;
using bsoncxx::builder::basic::sub_array;

Database::Database(Logger* l) {
    mongocxx::instance inst{};
    client = new mongocxx::client{mongocxx::uri{DB_URI}};
    logger = l;
    db = (*client)[DB_NAME];
    l->info(id, "connected to database");
}

bool Database::listUsers() {
    mongocxx::collection collection = db["users"];
    auto cursor = collection.find({});

    vector<User> users;

    for (auto&& doc : cursor) {
        User user;
//        std::cout << bsoncxx::to_json(doc) << std::endl;

        bool ok = true;

        vector<pair<string*, string> > stringFields;

        stringFields.emplace_back(make_pair(&user.username, "username"));
        stringFields.emplace_back(make_pair(&user.password, "password"));
        stringFields.emplace_back(make_pair(&user.name, "name"));
        stringFields.emplace_back(make_pair(&user.surname, "surname"));
        stringFields.emplace_back(make_pair(&user.home_dir, "homeDir"));

        for(auto field: stringFields) {
            auto val = doc[field.second];
            if (!val) {
                ok = false;
            } else {
                *(field.first) = bsoncxx::string::to_string(val.get_utf8().value);
            }
        }

        if(ok) {
            cout<<user.print()<<endl;
        } else {
            cout<<"wrong user"<<endl;
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
    }
}

bool Database::addUser(User& user) {
    mongocxx::collection collection = db["users"];

    auto doc = bsoncxx::builder::basic::document{};
    using bsoncxx::builder::basic::kvp;

    vector<pair<string*, string> > stringFields;

    stringFields.emplace_back(make_pair(&user.username, "username"));
    stringFields.emplace_back(make_pair(&user.password, "password"));
    stringFields.emplace_back(make_pair(&user.name, "name"));
    stringFields.emplace_back(make_pair(&user.surname, "surname"));
    stringFields.emplace_back(make_pair(&user.home_dir, "homeDir"));

    for(auto field: stringFields) {
        doc.append(kvp(field.second, *(field.first)));
    }

    collection.insert_one(doc.view());
}