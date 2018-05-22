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

bool Database::getField(string& colName, string& fieldName, bsoncxx::oid id, bsoncxx::document::element& el) {
    mongocxx::options::find opts{};
    opts.projection(make_document(kvp(fieldName, 1), kvp("_id", 0)));

    auto cursor = db[colName].find(make_document(kvp("_id", id)), opts);

    auto doc_i = cursor.begin();

    if (doc_i == cursor.end() || doc_i->empty()) {
        return false;
    }

    auto val = doc_i->begin();

    if (bsoncxx::string::to_string(val->key()) == fieldName) {
        el = *val;
    }
}

bool Database::getField(string&& colName, string&& fieldName, bsoncxx::oid id, bsoncxx::document::element& el) {
    return getField(colName, fieldName, id, el);
}

bool Database::getField(string&& colName, string&& fieldName, bsoncxx::oid id, string& res) {
    bsoncxx::document::element el;

    if(getField(colName, fieldName, id, el)) {
        if(el.type() == bsoncxx::type::k_utf8) {
            res = bsoncxx::string::to_string(el.get_utf8().value);
            return true;
        }
    }

    return false;
}

bool Database::getField(string&& colName, string&& fieldName, bsoncxx::oid id, int64_t& res) {
    bsoncxx::document::element el;

    if(getField(colName, fieldName, id, el)) {
        if(el.type() == bsoncxx::type::k_int64) {
            res = el.get_int64().value;
            return true;
        }
    }

    return false;
}

bool Database::getId(string&& colName, string&& fieldName, string&& fieldValue, bsoncxx::oid& id) {
    mongocxx::options::find opts{};
    opts.projection(make_document(kvp("_id", 1)));

    auto cursor = db[colName].find(make_document(kvp(fieldName, fieldValue)), opts);

    auto doc_i = cursor.begin();

    if (doc_i == cursor.end() || doc_i->empty()) {
        return false;
    }

    auto val = doc_i->begin();

    if (bsoncxx::string::to_string(val->key()) == fieldName) {
        if(val->type() == bsoncxx::type::k_oid) {
            id = val->get_oid().value;
            return true;
        }
    }
}

bool Database::getFields(string&& colName, bsoncxx::oid id, map<string, bsoncxx::document::element>& elements) {
    auto doc = bsoncxx::builder::basic::document{};
    doc.append(kvp("_id", 0));
//    auto fields = make_document();

    for(const auto &name: elements) {
        doc.append(kvp(name.first, 1));
    }

    mongocxx::options::find opts{};

    opts.projection(doc.view());

    auto cursor = db[colName].find(make_document(kvp("_id", id)), opts);

    auto doc_i = cursor.begin();

    if (doc_i == cursor.end() || doc_i->empty()) {
        return false;
    }

    if (distance(doc_i->begin(), doc_i->end()) != elements.size()) {
        return false;
    }

    for(auto val: (*doc_i)) {
        if(elements.find(bsoncxx::string::to_string(val.key())) == elements.end()) {
            // should not ever happen
            return false;
        }

        elements[bsoncxx::string::to_string(val.key())] = val;
    }

    return true;
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