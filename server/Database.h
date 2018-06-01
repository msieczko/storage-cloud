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
#include <bsoncxx/json.hpp>

#include <mongocxx/client.hpp>
#include <mongocxx/instance.hpp>


#define DB_URI "mongodb://localhost:27017"
#define DB_NAME "tin"

using std::string;

class Database {
private:
    mongocxx::client* client;
    mongocxx::database db;
    Logger* logger;
    std::string l_id = "DB";
    bool connected;
    mongocxx::instance* inst;

    bool getField(string&, string&, bsoncxx::oid, bsoncxx::document::element&);
    bool setField(string&, string&, bsoncxx::oid id, bsoncxx::types::value&);
public:
    Database(Logger*);
    ~Database();
    bool getField(string&&, string&&, bsoncxx::oid, bsoncxx::document::element&);
    bool getField(string&&, string&&, bsoncxx::oid, string&);
    bool getField(string&&, string&&, bsoncxx::oid, int64_t&);
    bool getField(string&&, string&&, bsoncxx::oid, const uint8_t*&, uint32_t&);
    bool getField(string&&, string&&, string&&, bsoncxx::oid& id, string&&, const string&, int64_t&);
    bool getId(string&&, string&&, const string&, bsoncxx::oid&);
    bool getFields(string&&, bsoncxx::oid, std::map<string, bsoncxx::document::element>&);
    bool getFields(string&&, std::vector<string>&, std::map<bsoncxx::oid, std::map<string, bsoncxx::document::element> >&);
    bool getFields(string&&, bsoncxx::document::value&&, std::vector<string>&,
            std::map<string, std::map<string, bsoncxx::document::element> >&);
    bool getFieldsAdvanced(string&&, mongocxx::pipeline&, std::vector<string>&, std::map<string, std::map<string, bsoncxx::document::element> >&);
    bool setField(string&&, string&&, bsoncxx::oid, bsoncxx::types::value&&);
    bool setField(string&, string&, bsoncxx::oid, bsoncxx::types::value&&);
    bool setField(string&&, string&&, bsoncxx::oid, string&);
    bool setField(string&&, string&&, bsoncxx::oid, int64_t&);
    bool setField(string&&, string&&, bsoncxx::oid, const uint8_t*, uint32_t);
    bool incField(string&&, string&&, string&&, bsoncxx::oid&, string&&, string&);
    bool countField(string&&, string&&, bsoncxx::oid, const uint8_t*, uint32_t, uint64_t&);
    bool countField(string&&, string&&, const string&, string&&, bsoncxx::oid, uint64_t&);
    bool removeFieldFromArray(string&&, string&&, bsoncxx::oid, bsoncxx::document::value&&);
    bool pushValToArr(string&&, string&&, bsoncxx::oid, bsoncxx::document::value&&);
    bool insertDoc(string&&, bsoncxx::oid&, bsoncxx::builder::basic::document&);
    static bsoncxx::types::b_binary stringToBinary(string&);
};

#endif //SERVER_DATABASE_H
