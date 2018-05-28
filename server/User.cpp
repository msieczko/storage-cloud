//
// Created by milosz on 24.05.18.
//

#include "User.h"

using namespace mongocxx;
using std::map;
using std::vector;

User::User(oid& id1, UserManager& u_m): id(id1), user_manager(u_m), authorized(false), valid(true) {}

User::User(UserManager& u_m):user_manager(u_m), authorized(false), valid(false) {}

User::User(const string& username, UserManager& u_m): user_manager(u_m), authorized(false), valid(false) {
    addUsername(username);
}

bool User::addUsername(const string &username) {
    oid tmp_id;

    if(!user_manager.getUserId(username, tmp_id)) {
        valid = false;
        authorized = false;
        return false;
    }

    if(valid) {
        if(tmp_id != id) {
            id = tmp_id;
            authorized = false;
        }
        return true;
    } else {
        id = tmp_id;
        authorized = false;
        valid = true;
    }
}

bool User::getName(string& name) {
    return user_manager.getName(id, name);
}

bool User::getSurname(string& name) {
    return user_manager.getSurname(id, name);
}

bool User::getHomeDir(string& dir) {
    return user_manager.getHomeDir(id, dir);
}

bool User::setName(string& name) {
    return user_manager.setName(id, name);
}

bool User::setPassword(string& passwd) {
    auto digest = new uint8_t[SHA512_DIGEST_LENGTH];

    SHA512((const uint8_t*) passwd.c_str(), passwd.size(), digest);

    string hash((char*)digest, SHA512_DIGEST_LENGTH);

    return user_manager.setPasswdHash(id, hash);
}

bool User::checkPassword(string& passwd) {
    auto digest = new uint8_t[SHA512_DIGEST_LENGTH];

    SHA512((const uint8_t*) passwd.c_str(), passwd.size(), digest);

    string hash_to_check((char*)digest, SHA512_DIGEST_LENGTH);
    string current_hash;

    if(!user_manager.getPasswdHash(id, current_hash)) {
        delete digest;
        return false;
    }

    bool wyn = (current_hash == hash_to_check);
    delete digest;
    return wyn;
}

bool User::loginByPassword(string& password, string& sid) {
    authorized = false;
    if(checkPassword(password)) {
        uint8_t t_sid_b[48];
        if(RAND_bytes(t_sid_b, 48) != 1) {
            return false;
        }

        string t_sid((char*)t_sid_b, 48);

        if(!user_manager.addSid(id, t_sid)) {
            return false;
        }

        sid = t_sid;
        authorized = true;
        return true;
    }

    return false;
}

bool User::loginBySid(string& sid) {
    authorized = user_manager.checkSid(id, sid);
    return authorized;
}

bool User::logout(string& sid) {
    return user_manager.removeSid(id, sid);
}


bool User::listFilesinPath(string& path, vector<string>& res) {
    return user_manager.listFilesinPath(id, path, res);
}

///---------------------UserManager---------------------

UserManager::UserManager(Database& db_t): db(db_t) {}

bool UserManager::getName(oid& id, string& res) {
    return db.getField("users", "name", id, res);
}

bool UserManager::getSurname(oid& id, string& res) {
    return db.getField("users", "surname", id, res);
}

bool UserManager::getHomeDir(oid& id, string& res) {
    return db.getField("users", "homeDir", id, res);
}

bool UserManager::setName(oid& id, string& res) {
    return db.setField("users", "name", id, res);
}

bool UserManager::getUserId(const string& username, oid& id) {
    return db.getId("users", "username", username, id);
}

bool UserManager::getPasswdHash(oid& id, string& res) {
    const uint8_t* tmp_b;
    uint32_t tmp_s;
    if(db.getField("users", "password", id, tmp_b, tmp_s)) {
        res = string((const char*) tmp_b, tmp_s);
        return true;
    }

    return false;
}

bool UserManager::setPasswdHash(oid& id, string& val) {
    return db.setField("users", "password", id, (const uint8_t*) val.c_str(), (uint32_t) val.size());
}

bool UserManager::addSid(oid& id, string& sid) {
    return db.pushValToArr("users", "sids", id, make_document(
            kvp("sid", Database::stringToBinary(sid)),
            kvp("time", bsoncxx::types::b_date(std::chrono::system_clock::now()))
    ));
}

bool UserManager::checkSid(oid& id, string& sid) {
    uint64_t tmp_l = 0;
    return (db.countField("users", "sids.sid", id, (const uint8_t*) sid.c_str(), sid.size(), tmp_l) && tmp_l == 1);
}

bool UserManager::removeSid(oid& id, string &sid) {
    return db.removeFieldFromArray("users", "sids", id, make_document(kvp("sid", Database::stringToBinary(sid))));
}

string UserManager::mapToString(map<string, bsoncxx::document::element>& in) {
    string wyn;
    for(auto &flds: in) {
        wyn += flds.first + ": " + bsoncxx::string::to_string(flds.second.get_utf8().value) + " ";
    }

    wyn.pop_back();

    return wyn;
}

bool UserManager::listAllUsers(std::vector<string>& res) {
    map<bsoncxx::oid, map<string, bsoncxx::document::element> > mmap;
    vector<string> fields;
    fields.emplace_back("username");
    fields.emplace_back("surname");
    fields.emplace_back("name");

    if(!db.getFields("users", fields, mmap)) {
        return false;
    }

    for(auto &usr: mmap) {
//        id: usr.first.to_string()
        res.emplace_back(mapToString(usr.second));
    }

    return true;
}

bool UserManager::listFilesinPath(oid& id, string& path, vector<string>& res) {

}

/**

db.getCollection('users').update(
{username: "miloszXD"},
{$push: {sids: {sid: "asdsasd", time: 12321}}})


db.getCollection('users').find({username: "miloszXD", "sids.sid": "asdsasd"})


db.getCollection('users').update({username: "miloszXD"}, {"$pull": {sids: {sid: "XDSID"}}})

**/
