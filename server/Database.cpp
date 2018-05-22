#include "Database.h"

#include <iostream>

#include <bsoncxx/builder/stream/document.hpp>
#include <bsoncxx/json.hpp>

#include <mongocxx/client.hpp>
#include <mongocxx/instance.hpp>

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

bool Database::getField(string colName, string fieldName, bsoncxx::oid id, string& res) {
    mongocxx::collection collection = db[colName];

    mongocxx::options::find opts{};
    opts.projection(make_document(kvp(fieldName, 1)));

    auto cursor = collection.find(make_document(kvp("_id", id)), opts);

    for (auto&& doc : cursor) {
        User user;
        std::cout << bsoncxx::to_json(doc) << std::endl;

//        user.fromBSON(doc);
//        user.print();
//        users.emplace_back(user);
    }
}

bool Database::listUsers(vector<User>& users) {
    mongocxx::collection collection = db["users"];
    auto cursor = collection.find({});

//    vector<User> users;

    for (auto&& doc : cursor) {
        User user;
//        std::cout << bsoncxx::to_json(doc) << std::endl;

        user.fromBSON(doc);
        user.print();
        users.emplace_back(user);
    }
}

bool Database::addUser(User& user) {
    mongocxx::collection collection = db["users"];

    auto doc = user.toBSON();

    collection.insert_one(doc.view());
}