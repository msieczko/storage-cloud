#include "Database.h"

#include <iostream>

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
        return true;
    }

    return false;
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

bool Database::getField(string&& colName, string&& fieldName, bsoncxx::oid id, const uint8_t*& res, uint32_t& resSize) {
    bsoncxx::document::element el;

    if(getField(colName, fieldName, id, el)) {
        if(el.type() == bsoncxx::type::k_binary) {
            res = el.get_binary().bytes;
            resSize = el.get_binary().size;
            return true;
        }
    }

    return false;
}

bool Database::getId(string&& colName, string&& fieldName, string& fieldValue, bsoncxx::oid& id) {
    mongocxx::options::find opts{};
    opts.projection(make_document(kvp("_id", 1)));

    auto cursor = db[colName].find(make_document(kvp(fieldName, fieldValue)), opts);

    auto doc_i = cursor.begin();

    if (doc_i == cursor.end() || doc_i->empty()) {
        return false;
    }

    auto val = doc_i->begin();

    if(val->type() == bsoncxx::type::k_oid) {
        id = val->get_oid().value;
        return true;
    }

    return false;
}

bool Database::getFields(string&& colName, bsoncxx::oid id, map<string, bsoncxx::document::element>& elements) {
    auto doc = bsoncxx::builder::basic::document{};
    doc.append(kvp("_id", 0));

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

bool Database::getFields(string&& colName, vector<string>& fields, map<bsoncxx::oid, map<string, bsoncxx::document::element> >& elements) {
    auto doc = bsoncxx::builder::basic::document{};

    for(const auto &name: fields) {
        doc.append(kvp(name, 1));
    }

    mongocxx::options::find opts{};
    opts.projection(doc.view());

    auto cursor = db[colName].find(make_document(), opts);

    bool notEmpty = false;

    for(auto doc_v: cursor) {
        notEmpty = true;

        if (distance(doc_v.begin(), doc_v.end()) != fields.size() + 1) {
            cout<<"err1, "<<distance(doc_v.begin(), doc_v.end())<<endl;
            return false;
        }

        bsoncxx::document::element t_id = doc_v["_id"];

        if (!t_id || t_id.type() != bsoncxx::type::k_oid) {
            cout<<"err2"<<endl;
            return false;
        }

        bsoncxx::oid id = t_id.get_oid().value;

        elements.emplace(id, map<string, bsoncxx::document::element>{});

        for(auto val: doc_v) {
            string key = bsoncxx::string::to_string(val.key());
            if (key != "_id") {
                elements[id].emplace(key, val);
            }
        }
    }

    return notEmpty;
}

bool Database::setField(string& colName, string& fieldName, bsoncxx::oid id, bsoncxx::types::value& val) {
    try {
        db[colName].update_one(make_document(kvp("_id", id)),
                               make_document(kvp("$set", make_document(kvp(fieldName, val)))));
    } catch (...) {
        return false;
    }

    return true;
}

bool Database::setField(string& colName, string& fieldName, bsoncxx::oid id, bsoncxx::types::value&& val) {
    return setField(colName, fieldName, id, val);
}

bool Database::setField(string&& colName, string&& fieldName, bsoncxx::oid id, bsoncxx::types::value&& val) {
    return setField(colName, fieldName, id, val);
}

bool Database::setField(string&& colName, string&& fieldName, bsoncxx::oid id, string& newVal) {
    return setField(colName, fieldName, id, bsoncxx::types::value{bsoncxx::types::b_utf8{newVal}});
}

bool Database::setField(string&& colName, string&& fieldName, bsoncxx::oid id, const uint8_t* newVal, uint32_t newValSize) {
    bsoncxx::types::b_binary b_val{};
    b_val.bytes = newVal;
    b_val.size = newValSize;
    return setField(colName, fieldName, id, bsoncxx::types::value{b_val});
}

bool Database::setField(string&& colName, string&& fieldName, bsoncxx::oid id, int64_t& newVal) {
    bsoncxx::types::b_int64 tmp{};
    tmp.value = newVal;
    return setField(colName, fieldName, id, bsoncxx::types::value{tmp});
}

bool Database::pushValToArr(string&& colName, string&& arrayName, bsoncxx::oid id, bsoncxx::document::value&& val) {
    try {
        db[colName].update_one(make_document(kvp("_id", id)),
                               make_document(kvp("$push", make_document(kvp(arrayName, val)))));
    } catch (...) {
        return false;
    }

    return true;
}

bool Database::insertDoc(string&& colName, bsoncxx::oid& id, map<string, bsoncxx::types::value>& elements) {
    auto doc = bsoncxx::builder::basic::document{};

    for(const auto &elem: elements) {
        doc.append(kvp(elem.first, elem.second));
    }

    try {
        auto res = db[colName].insert_one(doc.view());

        if(!res) {
            return false;
        }

        if (res->inserted_id().type() != bsoncxx::type::k_oid) {
            return false;
        }

        id = res->inserted_id().get_oid().value;
        return true;

    } catch (...) {
        return false;
    }
}