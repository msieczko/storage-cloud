//
// Created by milosz on 24.05.18.
//

#include "User.h"

User::User(oid& id1, UserManager& u_m): id(id1), user_manager(u_m) {}

User::User(string& username, UserManager& u_m): user_manager(u_m) {
    if(!user_manager.getUserId(username, id)) {
        std::cout<<"user not found"<<std::endl;
    }
}

bool User::getName(string& name) {
    return user_manager.getName(id, name);
}

bool User::getSurname(string& name) {
    return user_manager.getSurname(id, name);
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
        return false;
    }

    return current_hash == hash_to_check;
}

///---------------------UserManager---------------------

UserManager::UserManager(Database& db_t): db(db_t) {}

bool UserManager::getName(oid& id, string& res) {
    return db.getField("users", "name", id, res);
}

bool UserManager::getSurname(oid& id, string& res) {
    return db.getField("users", "surname", id, res);
}

bool UserManager::setName(oid& id, string& res) {
    return db.setField("users", "name", id, res);
}

bool UserManager::getUserId(string& username, oid& id) {
    return db.getId("users", "username", username, id);
}

bool UserManager::getPasswdHash(oid& id, string& res) {
    return db.getField("users", "password", id, res);
}

bool UserManager::setPasswdHash(oid& id, string& res) {
    return db.setField("users", "password", id, res);
}
/**

db.getCollection('users').update(
{username: "miloszXD"},
{$push: {sids: {sid: "asdsasd", time: 12321}}})


db.getCollection('users').find({username: "miloszXD", "sids.sid": "asdsasd"})

**/
