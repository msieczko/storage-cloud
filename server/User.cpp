//
// Created by milosz on 24.05.18.
//

#include "User.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fstream>

using namespace mongocxx;
using std::map;
using std::vector;

using bsoncxx::builder::basic::make_array;

User::User(oid& id1, UserManager& u_m): id(id1), user_manager(u_m), authorized(false), valid(true), currentInFileValid(false) {}

User::User(UserManager& u_m):user_manager(u_m), authorized(false), valid(false), currentInFileValid(false) {}

User::User(const string& username, UserManager& u_m): user_manager(u_m), authorized(false), valid(false), currentInFileValid(false) {
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
    return user_manager.setPasswd(id, passwd);
}

bool User::checkPassword(string& passwd) {
    return user_manager.checkPasswd(id, passwd);
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
    valid = authorized = false;
    return user_manager.removeSid(id, sid);
}


bool User::listFilesinPath(const string& path, vector<UFile>& res) {
    return user_manager.listFilesinPath(id, path, res);
}

// also adds directory
uint8_t User::addFile(UFile& file) {
    currentInFileValid = false;
    if(file.filename[0] != '/') {
        return ADD_FILE_WRONG_DIR;
    }

    uint64_t pos;

    if((pos = file.filename.rfind('/')) == string::npos) {
        return ADD_FILE_WRONG_DIR;
    }

    if(pos == file.filename.size() - 1) {
        return ADD_FILE_EMPTY_NAME;
    }

    string dir = file.filename.substr(0, pos);

    if(!dir.empty() && !user_manager.yourFileIsDir(id, dir)) {
        return ADD_FILE_WRONG_DIR;
    }

    if(user_manager.yourFileExists(id, file.filename)) {
        if(file.type == FILE_REGULAR) {
            UFile tmp_file;
            if(user_manager.getYourFileMetadata(id, file.filename, tmp_file)) {
                if(!tmp_file.isValid && tmp_file.size == file.size && tmp_file.hash == file.hash) {
                    currentInFile = tmp_file;
                    currentInFileValid = true;
                    return ADD_FILE_CONTINUE_OK;
                }
            }
        }

        return ADD_FILE_FILE_EXISTS;
    }

    oid fileId;

    if(user_manager.addNewFile(id, file, dir, fileId)) {
        if(file.type == FILE_REGULAR) {
            currentInFile = file;
            currentInFile.isValid = false;
            currentInFile.lastValid = 0;
            currentInFile.id = fileId;
            currentInFileValid = true;
        }

        return ADD_FILE_OK;
    }

    return ADD_FILE_INTERNAL_ERROR;
}

const UFile& User::getCurrentInFileMetadata() {
    return currentInFile;
}

bool User::isCurrentInFileValid() {
    return currentInFileValid;
}

bool User::addFileChunk(const string& chunk) {
    if(!currentInFileValid) {
        return false;
    }

    if(currentInFile.size > currentInFile.lastValid + chunk.size()) {
        return false;
    }

    if(!user_manager.addFileChunk(currentInFile, chunk)) {
        return false;
    }

    if(currentInFile.size != currentInFile.lastValid) {
        return true;
    }

    return user_manager.validateFile(currentInFile);
    //TODO dodac usuwanie jak nie pyknie
}

bool User::isAdmin() {
    if(valid && authorized) {
        uint8_t role;
        if(user_manager.getUserRole(id, role) && role == USER_ADMIN) {
            return true;
        }
    }

    return false;
}

///---------------------UserManager---------------------

UserManager::UserManager(Database& db_t, Logger& logger_t): db(db_t), logger(logger_t) {}

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

bool UserManager::getUserRole(oid& id, uint8_t& role) {
    return db.getField("users", "role", id, (int64_t&)role);
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

bool UserManager::checkPasswd(oid& id, string& passwd) {
    auto digest = new uint8_t[SHA512_DIGEST_LENGTH];

    SHA512((const uint8_t*) passwd.c_str(), passwd.size(), digest);

    string hash_to_check((char*)digest, SHA512_DIGEST_LENGTH);
    string current_hash;

    if(!getPasswdHash(id, current_hash)) {
        delete digest;
        return false;
    }

    bool wyn = (current_hash == hash_to_check);
    delete digest;

    return wyn;
}

bool UserManager::setPasswd(oid& id, const string& passwd) {
    auto digest = new uint8_t[SHA512_DIGEST_LENGTH];

    SHA512((const uint8_t*) passwd.c_str(), passwd.size(), digest);

    string hash((char*)digest, SHA512_DIGEST_LENGTH);

    return db.setField("users", "password", id, (const uint8_t*) hash.c_str(), (uint32_t) hash.size());
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

//string UserManager::mapToString(map<string, bsoncxx::document::element>& in) {
//    string wyn;
//    for(auto &flds: in) {
//        if(flds.second.type() == bsoncxx::type::k_utf8) {
//            wyn += flds.first + ": " + bsoncxx::string::to_string(flds.second.get_utf8().value) + " ";
//        } else if(flds.second.type() == bsoncxx::type::k_int64) {
//            wyn += flds.first + ": " + std::to_string(flds.second.get_int64().value) + " ";
//        } else {
//            wyn += flds.first + ": unknown type ";
//        }
//    }
//
//    wyn.pop_back();
//
//    return wyn;
//}

bool UserManager::parseUserDetails(map<string, bsoncxx::types::value>& usr, UDetails& tmp_u) {
    try {
        tmp_u.username = bsoncxx::string::to_string(usr.find("username")->second.get_utf8().value);
        tmp_u.name = bsoncxx::string::to_string(usr.find("name")->second.get_utf8().value);
        tmp_u.surname = bsoncxx::string::to_string(usr.find("surname")->second.get_utf8().value);
        tmp_u.role = (uint8_t) usr.find("role")->second.get_int64().value;
        tmp_u.totalSpace = (uint64_t) usr.find("totalSpace")->second.get_int64().value;
        uint64_t freeSpace = (uint64_t) usr.find("freeSpace")->second.get_int64().value;
        tmp_u.usedSpace = tmp_u.totalSpace - freeSpace;
    } catch (const std::exception& ex) {
        logger.err(l_id, "error while parsing user details: " + string(ex.what()));
        return false;
    } catch (...) {
        logger.err(l_id, "error while parsing user details: unknown error");
        return false;
    }
}

bool UserManager::getUserDetails(oid id, UDetails& userDetails) {
    map<string, bsoncxx::types::value> mmap;
    vector<string> fields{"username", "surname", "name", "role", "totalSpace", "freeSpace"};
    for(string field: fields) {
        mmap.emplace(field, bsoncxx::types::value(bsoncxx::types::b_null{}));
    }

    if(!db.getFields("users", id, mmap)) {
        return false;
    }

    return parseUserDetails(mmap, userDetails);
}

bool UserManager::registerUser(UDetails& user, const string& password, bool& userTaken) {
    uint64_t userCount = 1;
    userTaken = false;
    if(!(db.countField("users", "username", user.username, userCount) && userCount == 0)) {
        userTaken = true;
        return false;
    }

    auto doc = bsoncxx::builder::basic::document{};
    doc.append(kvp("username", toUTF8(user.username)));
    doc.append(kvp("name", toUTF8(user.name)));
    doc.append(kvp("surname", toUTF8(user.surname)));
    doc.append(kvp("role", toINT64(user.role)));

    if(user.role != USER_ADMIN) {
        doc.append(kvp("totalStorage", toINT64(defaultStorage)));
        doc.append(kvp("freeStorage", toINT64(defaultStorage)));

        string homeDir = "/" + user.username;

        doc.append(kvp("homeDir", toUTF8(homeDir)));

        string fullPath = root_path + homeDir;

        int mkdir_res = mkdir(fullPath.c_str(), S_IRWXU);

        if(mkdir_res != 0) {
            logger.err(l_id, "error while trying to mkdir " + fullPath + " for new user");
            return false;
        }
    } else {
        doc.append(kvp("totalStorage", toINT64(0)));
        doc.append(kvp("freeStorage", toINT64(0)));
        doc.append(kvp("homeDir", ""));
    }

    oid tmp_id;

    if(!db.insertDoc("users", tmp_id, doc)) {
        return false;
    }

    setPasswd(tmp_id, password);
}

bool UserManager::listAllUsers(std::vector<UDetails>& res) {
    map<bsoncxx::oid, map<string, bsoncxx::types::value> > mmap;
    vector<string> fields{"username", "surname", "name", "role", "totalSpace", "freeSpace"};

    if(!db.getFields("users", fields, mmap)) {
        return false;
    }

    for(auto &usr: mmap) {
//        id: usr.first.to_string()
//        res.emplace_back(mapToString(usr.second));
        UDetails tmp_u;
        if(parseUserDetails(usr.second, tmp_u)) {
            res.emplace_back(tmp_u);
        } else {
            return false;
        }
    }

    return true;
}

bool UserManager::listFilesinPath(oid& id, const string& path, vector<UFile>& files) {
//    return false;
    map<string, map<string, bsoncxx::types::value> > mmap;
    vector<string> fields;
    fields.emplace_back("filename");
    fields.emplace_back("size");
    fields.emplace_back("creationDate");
    fields.emplace_back("ownerName");
    fields.emplace_back("type");
    fields.emplace_back("hash");
//    fields.emplace_back("isValid");

//    map<string, bsoncxx::types::value> queryValues;
//    queryValues.emplace("owner", id);
//    queryValues.emplace("filename", bsoncxx::types::b_regex(R"(\/dir\/nice\/[^\/]+)"));

    string parsedPath = path;

    if(parsedPath[parsedPath.size()-1] != '/') {
        parsedPath.push_back('/');
    }

    mongocxx::pipeline stages;

    stages.match(make_document(kvp("owner", id), kvp("isValid", true), kvp("filename", bsoncxx::types::b_regex("^"+parsedPath+"[^/]*[^/]$"))));
    stages.lookup(make_document(kvp("from", "users"), kvp("localField", "owner"), kvp("foreignField", "_id"), kvp("as", "ownerTMP")));
    stages.unwind("$ownerTMP");
    stages.add_fields(make_document(kvp("ownerName", make_document(kvp("$concat", make_array("$ownerTMP.name", " ", "$ownerTMP.surname"))))));
    stages.project(make_document(kvp("ownerTMP", 0), kvp("_id", 0)));

    if(!db.getFieldsAdvanced("files", stages, fields, mmap)) {
        return false;
    }

    try {
        for(std::pair<string, map<string, bsoncxx::types::value> > usr: mmap) {
    //        id: usr.first.to_string()
    //        res.emplace_back(mapToString(usr.second));

            UFile tmp;
            tmp.filename = usr.first;
            tmp.size = (uint64_t) usr.second.find("size")->second.get_int64();
            tmp.creation_date = (uint64_t) usr.second.find("creationDate")->second.get_date().to_int64();
            tmp.owner_name = bsoncxx::string::to_string(usr.second.find("ownerName")->second.get_utf8().value);
            tmp.type = (uint8_t) usr.second.find("type")->second.get_int64().value;
            tmp.hash = string((const char*) usr.second.find("hash")->second.get_binary().bytes, usr.second.find("hash")->second.get_binary().size);

            files.emplace_back(tmp);
        }
    } catch (const std::exception& ex) {
        logger.err(l_id, "error while parsing file list: " + string(ex.what()));
        return false;
    } catch (...) {
        logger.err(l_id, "error while parsing file list: unknown error");
        return false;
    }

    return true;
}

bsoncxx::types::b_utf8 UserManager::toUTF8(string& s) {
    return bsoncxx::types::b_utf8(s);
}

bsoncxx::types::b_int64 UserManager::toINT64(uint64_t i) {
    bsoncxx::types::b_int64 tmp{};
    tmp.value = i;
    return tmp;
}

bsoncxx::types::b_date UserManager::currDate() {
    return bsoncxx::types::b_date(std::chrono::system_clock::now());
}

bsoncxx::types::b_oid UserManager::toOID(oid i) {
    bsoncxx::types::b_oid tmp{};
    tmp.value = i;
    return tmp;
}

bsoncxx::types::b_bool UserManager::toBool(bool b) {
    bsoncxx::types::b_bool tmp{};
    tmp.value = b;
    return tmp;
}

bsoncxx::types::b_binary UserManager::toBinary(string& str) {
    bsoncxx::types::b_binary b_sid{};
    b_sid.bytes = (const uint8_t*) str.c_str();
    b_sid.size = (uint32_t) str.size();
    return b_sid;
}

// also adds directory
bool UserManager::addNewFile(oid& id, UFile& file, string& dir, oid& newId) {
    auto doc = bsoncxx::builder::basic::document{};

    //TODO increase file count in parent dir

    if(file.type == FILE_REGULAR) {
        doc.append(kvp("filename", toUTF8(file.filename)));
        doc.append(kvp("size", toINT64(file.size)));
        doc.append(kvp("creationDate", currDate()));
        doc.append(kvp("type", toINT64(FILE_REGULAR)));
        doc.append(kvp("hash", toBinary(file.hash)));
        doc.append(kvp("isValid", toBool(false)));
        doc.append(kvp("lastValid", toINT64(0)));
        doc.append(kvp("owner", toOID(id)));

        string homeDir;

        if(!getHomeDir(id, homeDir)) {
            return false;
        }

        string fullPath = root_path + homeDir + file.filename;

        std::fstream fs;
        fs.open(fullPath, std::ios::out);

        if(!fs.is_open()) {
            //TODO log it;
            return false;
        }

        fs.close();

        if(!db.insertDoc("files", newId, doc)) {
            remove(fullPath.c_str());
            return false;
        }

        file.realPath = fullPath;
    } else if(file.type == FILE_DIR) {
        string tmp = "";
        doc.append(kvp("filename", toUTF8(file.filename)));
        doc.append(kvp("size", toINT64(0)));
        doc.append(kvp("creationDate", currDate()));
        doc.append(kvp("type", toINT64(FILE_DIR)));
        doc.append(kvp("owner", toOID(id)));
        doc.append(kvp("hash", toBinary(tmp)));
        doc.append(kvp("isValid", toBool(true)));

        string homeDir;

        if(!getHomeDir(id, homeDir)) {
            return false;
        }

        string fullPath = root_path + homeDir + file.filename;
        int mkdir_res = mkdir(fullPath.c_str(), S_IRWXU);

        if(mkdir_res != 0) {
            logger.err(l_id, "mkdir error while trying to create directory: " + fullPath);
            return false;
        }

        if(!db.insertDoc("files", newId, doc)) {
            rmdir(fullPath.c_str());
            return false;
        }

        file.realPath = fullPath;
    } else {
        return false;
    }

    // if not root dir
    if(!dir.empty()) {
        db.incField("files", "size", "owner", id, "filename", dir);
    }

    return true;
}

bool UserManager::yourFileExists(oid &id, const string &filename) {
    uint64_t tmp_l = 0;
    return (db.countField("files", "filename", filename, "owner", id, tmp_l) && tmp_l == 1);
}

bool UserManager::yourFileIsDir(oid &id, const string &path) {
    int64_t type;
    if(!db.getField("files", "type", "owner", id, "filename", path, type)) {
        return false;
    }

    return type == FILE_DIR;
}

bool UserManager::getYourFileMetadata(oid& id, const string& filename, UFile& file) {
    map<string, map<string, bsoncxx::types::value> > mmap;
    vector<string> fields;
    fields.emplace_back("filename");
    fields.emplace_back("size");
    fields.emplace_back("creationDate");
    fields.emplace_back("ownerName");
    fields.emplace_back("type");
    fields.emplace_back("hash");
    fields.emplace_back("isValid");
    fields.emplace_back("lastValid");
    fields.emplace_back("_id");
    
    mongocxx::pipeline stages;

    stages.match(make_document(kvp("owner", id), kvp("filename", filename), kvp("type", FILE_REGULAR)));
    stages.lookup(make_document(kvp("from", "users"), kvp("localField", "owner"), kvp("foreignField", "_id"), kvp("as", "ownerTMP")));
    stages.unwind("$ownerTMP");
    stages.add_fields(make_document(kvp("ownerName", make_document(kvp("$concat", make_array("$ownerTMP.name", " ", "$ownerTMP.surname"))))));
    stages.project(make_document(kvp("ownerTMP", 0)));

    if(!db.getFieldsAdvanced("files", stages, fields, mmap)) {
        return false;
    }
    
    if(mmap.size() != 1) {
        //TODO log
        return false;
    }

    try {
        auto tmp_file = *(mmap.begin());

        file.filename = bsoncxx::string::to_string(tmp_file.second.find("filename")->second.get_utf8().value);
        file.size = (uint64_t) tmp_file.second.find("size")->second.get_int64().value;
        file.creation_date = (uint64_t) tmp_file.second.find("creationDate")->second.get_date().to_int64();
        file.owner_name = bsoncxx::string::to_string(tmp_file.second.find("ownerName")->second.get_utf8().value);
        file.type = (uint8_t) tmp_file.second.find("type")->second.get_int64().value;
        file.hash = string((const char*) tmp_file.second.find("hash")->second.get_binary().bytes, tmp_file.second.find("hash")->second.get_binary().size);
        file.isValid = tmp_file.second.find("isValid")->second.get_bool();
        file.lastValid = (uint64_t) tmp_file.second.find("lastValid")->second.get_int64();
        file.id = tmp_file.second.find("_id")->second.get_oid().value;

        string homeDir;

        if(!getHomeDir(id, homeDir)) {
            return false;
        }

        file.realPath = root_path + homeDir + file.filename;
    } catch (const std::exception& ex) {
        logger.err(l_id, "error while parsing file metadata: " + string(ex.what()));
        return false;
    } catch (...) {
        logger.err(l_id, "error while parsing file metadata: unknown error");
        return false;
    }

    return true;
}

bool UserManager::addFileChunk(UFile& file, const string& chunk) {
    std::fstream fs;
    if(file.lastValid == 0) {
        fs.open(file.realPath, std::ios::out | std::ios::binary | std::ios::trunc);
    } else {
        fs.open(file.realPath, std::ios::out | std::ios::binary | std::ios::in);
    }
    if(!fs.is_open()) {
        return false;
    }
    fs.seekp(file.lastValid, std::ios::beg);

    fs.write(chunk.c_str(), chunk.size());
    if(fs.bad()) {
        return false;
    }

    fs.close();

    file.lastValid += chunk.size();

    return db.incField("files", file.id, "lastValid", chunk.size());
}

bool UserManager::validateFile(UFile& file) {
    uint8_t hash[FILE_HASH_SIZE];
    SHA_CTX sha1;
    SHA1_Init(&sha1);
    const int bufSize = 32768;
    uint8_t* buffer = new uint8_t[bufSize];

    std::ifstream is (file.realPath, std::ios::binary | std::ios::in);
    if(!is.is_open()) {
        return false;
    }

    is.seekg (0, is.end);
    uint64_t length = is.tellg();
    is.seekg (0, is.beg);

    if(length != file.size) {
        return false;
    }

    do {
        is.read((char*) buffer, bufSize);
        if(is.bad()) {
            break;
        }
        SHA1_Update(&sha1, buffer, is.gcount());
    } while(!is.eof());

    delete buffer;

    if(!is.eof()) {
        return false;
    }

    SHA1_Final(hash, &sha1);

    is.close();

    for(int i=0; i<FILE_HASH_SIZE; i++) {
        if((uint8_t) file.hash[i] != hash[i]) {
            return false;
        }
    }

    if(!db.setField("files", "isValid", file.id, true)) {
        return false;
    }

    file.isValid = true;

    return true;
}

/**

db.getCollection('users').update(
{username: "miloszXD"},
{$push: {sids: {sid: "asdsasd", time: 12321}}})


db.getCollection('users').find({username: "miloszXD", "sids.sid": "asdsasd"})


db.getCollection('users').update({username: "miloszXD"}, {"$pull": {sids: {sid: "XDSID"}}})

**/
