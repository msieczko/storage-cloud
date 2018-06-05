#include "Client.h"

using namespace std;
using namespace StorageCloud;

void Client::resError(ServerResponse& res, string&& reason, string&& loggerReason) {
    res.set_type(ResponseType::ERROR);
    Param* tmp_param = res.add_params();
    tmp_param->set_paramid("msg");
    tmp_param->set_sparamval(reason);
    logger->log(id, "client " + username + " " + loggerReason);
}

bool Client::processCommand(Command* cmd) {
    logger->log(id, "Received command '" + CommandType_Name(cmd->type()) + "' (" + to_string(cmd->type()) +
                    "), with " + to_string(cmd->params_size()) + " params");

    ServerResponse res;

    if(cmd->type() == CommandType::LOGIN) {
        bool params_ok = (cmd->params_size() == 2);
        bool alreadyAuthorized = u.isAuthorized();
        string sid;
        string passwd, t_username;

        if(params_ok && !alreadyAuthorized) {
            sessionId = "";
            if(cmd->params(0).paramid() == "username" && cmd->params(1).paramid() == "password") {
                passwd = cmd->params(1).sparamval();
                t_username = cmd->params(0).sparamval();
            } else if (cmd->params(0).paramid() == "password" && cmd->params(1).paramid() == "username") {
                passwd = cmd->params(0).sparamval();
                t_username = cmd->params(1).sparamval();
            } else {
                params_ok = false;
            }

            if(params_ok) {
                if(u.addUsername(t_username)) {
                    u.loginByPassword(passwd, sid);
                }
            }
        }

        if(params_ok && u.isAuthorized()) {
            username = t_username;
            sessionId = sid;
            res.set_type(ResponseType::LOGGED);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("sid");
            tmp_param->set_bparamval(sid);
            logger->log(id, "user " + t_username + " logged in");
        } else {
            res.set_type(ResponseType::ERROR);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("msg");

            if(params_ok) {
                tmp_param->set_sparamval("Invalid username/password");
                if(!u.isValid()) {
                    logger->log(id, "client " + t_username + " tried to log in, but that user doesn't exist");
                } else if(!u.isAuthorized()) {
                    logger->log(id, "client " + t_username + " tried to log in, but provided wrong password");
                } else {
                    logger->warn(id, "client " + t_username + " tried to log in, but internal error occurred");
                }
            } else {
                if(!alreadyAuthorized) {
                    tmp_param->set_sparamval("Invalid command format");
                    logger->log(id, "client send login command, but command format was wrong");
                } else {
                    tmp_param->set_sparamval("You have to logout first");
                    logger->log(id, "client tried to login, but is already logged in");
                }
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::RELOGIN) {
        bool params_ok = (cmd->params_size() == 2);
        bool alreadyAuthorized = u.isAuthorized();
        string sid, t_username;

        if(params_ok && !alreadyAuthorized) {
            sessionId = "";
            if(cmd->params(0).paramid() == "username" && cmd->params(1).paramid() == "sid") {
                sid = cmd->params(1).bparamval();
                t_username = cmd->params(0).sparamval();
            } else if (cmd->params(0).paramid() == "sid" && cmd->params(1).paramid() == "username") {
                sid = cmd->params(0).bparamval();
                t_username = cmd->params(1).sparamval();
            } else {
                params_ok = false;
            }

            if(params_ok) {
                if(u.addUsername(t_username)) {
                    u.loginBySid(sid);
                }
            }
        }

        if(params_ok && u.isAuthorized()) {
            username = t_username;
            sessionId = sid;
            res.set_type(ResponseType::LOGGED);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("sid");
            tmp_param->set_bparamval(sid);
            logger->log(id, "user " + t_username + " relogged in");
        } else {
            res.set_type(ResponseType::ERROR);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("msg");

            if(params_ok) {
                tmp_param->set_sparamval("Invalid username or session ID");
                if(!u.isValid()) {
                    logger->log(id, "client " + t_username + " tried to relogin, but that user doesn't exist");
                } else if(!u.isAuthorized()) {
                    logger->log(id, "client " + t_username + " tried to relogin, but provided wrong sid");
                } else {
                    logger->warn(id, "client " + t_username + " tried to relogin, but internal error occurred");
                }
            } else {
                if(!alreadyAuthorized) {
                    tmp_param->set_sparamval("Invalid command format");
                    logger->log(id, "client send relogin command, but command format was wrong");
                } else {
                    tmp_param->set_sparamval("You have to logout first");
                    logger->log(id, "client tried to relogin, but is already logged in");
                }
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::LOGOUT) {
        if(!(u.isValid() && u.isAuthorized())) {
            res.set_type(ResponseType::ERROR);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("msg");
            tmp_param->set_sparamval("You are not logged in");
            logger->log(id, "client " + username + " tried to log out, but was not logged in");
        } else {
            u.logout(sessionId);
            res.set_type(ResponseType::OK);
            logger->log(id, "client " + username + " logged out");
            sessionId = "";
            username = "";
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::LIST_FILES) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to list files, but was not logged in");
        } else {
            if(cmd->params_size() == 1 && cmd->params(0).paramid() == "path") {
                vector<UFile> files;

                if(!u.listFilesinPath(cmd->params(0).sparamval(), files)) {
                    resError(res, "Internal error occured", "tried to list files, but internal error occured");
                } else {

                    for(auto &&file: files) {
                        File *tmp_file = res.add_filelist();
                        tmp_file->set_filename(file.filename);
                        tmp_file->set_filetype(file.type == FILE_REGULAR ? FileType::FILE : FileType::DIRECTORY);
                        tmp_file->set_size(file.size);
                        tmp_file->set_hash(file.hash);
                        tmp_file->set_owner(file.owner_name);
                        tmp_file->set_creationdate(file.creation_date);
                        tmp_file->set_isshared(file.isShared);
                    }

                    res.set_type(ResponseType::FILES);
                }
            } else {
                resError(res, "Wrong command format", "tried to list files, but command format was wrong");
            }

        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::METADATA) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to add metadata, but was not logged in");
        } else {
            string path, hash;
            uint64_t size = 0;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "target_file_path") {
                    path = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "file_checksum") {
                    hash = param.bparamval();
                    validFields++;
                } else if(param.paramid() == "size") {
                    size = param.iparamval();
                    validFields++;
                }
            }

            bool paramsOk = false;

            if(validFields == 3) {
                if(path.size() > 0 && hash.size() == FILE_HASH_SIZE && size < 1024ull*1024ull*1024ull*50ull) {
                    paramsOk = true;
                }
            }

            if(paramsOk) {
                UFile file;
                file.filename = path;
                file.type = FILE_REGULAR;
                file.hash = hash;
                file.size = size;

                uint8_t wyn = u.addFile(file);

                if(wyn == ADD_FILE_OK) {
                    res.set_type(ResponseType::CAN_SEND);
                    Param* tmp = res.add_params();
                    tmp->set_paramid("starting_chunk");
                    tmp->set_iparamval(0);
                } else if(wyn == ADD_FILE_CONTINUE_OK) {
                    UFile tmp_file = u.getCurrentInFileMetadata();
                    if(!u.isCurrentInFileValid()) {
                        resError(res, "Internal error occured (2)", "tried to add metadata, tried to continue, but internal error occured");
                        logger->err(id, "client " + username + " tried to add metadata, tried to continue, but internal error occured");
                    } else {
                        Param* tmp = res.add_params();
                        tmp->set_paramid("starting_chunk");
                        tmp->set_iparamval(tmp_file.lastValid);
                    }
                } else {
                    if(wyn == ADD_FILE_INTERNAL_ERROR) {
                        resError(res, "Internal error occured", "tried to add metadata, but internal error occured");
                    } else if(wyn == ADD_FILE_WRONG_DIR) {
                        resError(res, "Wrong path", "tried to add metadata, but provided wrong path");
                    } else if(wyn == ADD_FILE_EMPTY_NAME) {
                        resError(res, "Filename empty", "tried to add metadata, but provided empty filename");
                    } else if(wyn == ADD_FILE_FILE_EXISTS) {
                        resError(res, "File already exists", "tried to add metadata, but filename already exists");
                    } else if(wyn == ADD_FILE_NO_SPACE) {
                        resError(res, "Not enough space left", "tried to add metadata, but doesn't have enough free space");
                    } else {
                        resError(res, "Unknown error", "tried to add metadata, but unknown error occured");
                    }
                }
            } else {
                resError(res, "Wrong command format", "tried to add metadata, but command format was wrong");
            }

        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::MKDIR) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to make directory, but was not logged in");
        } else {
            if(cmd->params_size() == 1 && cmd->params(0).paramid() == "path" && !cmd->params(0).sparamval().empty()) {
                UFile file;
                file.filename = cmd->params(0).sparamval();
                file.type = FILE_DIR;

                uint8_t wyn = u.addFile(file);

                if(wyn == ADD_FILE_OK) {
                    res.set_type(ResponseType::OK);
                } else {
                    if(wyn == ADD_FILE_INTERNAL_ERROR) {
                        resError(res, "Internal error occured", "tried to make directory, but internal error occured");
                    } else if(wyn == ADD_FILE_WRONG_DIR) {
                        resError(res, "Wrong path", "tried to make directory, but provided wrong path");
                    } else if(wyn == ADD_FILE_EMPTY_NAME) {
                        resError(res, "Filename empty", "tried to make directory, but provided empty filename");
                    } else if(wyn == ADD_FILE_FILE_EXISTS) {
                        resError(res, "Directory already exists", "tried to make directory, but directory already exists");
                    } else {
                        resError(res, "Unknown error", "tried to make directory, but unknown error occured");
                    }
                }
            } else {
                resError(res, "Wrong command format", "tried to make directory, but command format was wrong");
            }

        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::USR_DATA) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to put data, but was not logged in");
        } else {
            if(cmd->params_size() == 1 && cmd->params(0).paramid() == "data" && cmd->params(0).bparamval().length()) {
                if(u.addFileChunk(cmd->params(0).bparamval())) {
                    if(u.getCurrentInFileMetadata().isValid) {
                        logger->log(id, "user " + username + ": adding file accomplished");
                    }
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to put data, but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to put data, but command format was wrong");
            }

        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::LIST_USERS) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to list users, but was not logged as admin");
        } else {
            vector<UDetails> userDetails;
            if(UserManager::getInstance().listAllUsers(userDetails)) {
                for(auto& user: userDetails) {
                    UserDetails* tmp = res.add_userlist();
                    tmp->set_firstname(user.name);
                    tmp->set_lastname(user.surname);
                    tmp->set_role(user.role == USER_ADMIN ? UserRole::ADMIN : UserRole::USER);
                    tmp->set_totalspace(user.totalSpace);
                    tmp->set_usedspace(user.usedSpace);
                    tmp->set_username(user.username);
                }

                res.set_type(ResponseType::USERS);
            } else {
                resError(res, "Error occured", "tried to list users, but error occured");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::REGISTER) {
        string username, passwd, name, surname;
        uint8_t validFields = 0;

        for(auto& param: cmd->params()) {
            if(param.paramid() == "username") {
                username = param.sparamval();
                validFields++;
            } else if(param.paramid() == "pass") {
                passwd = param.sparamval();
                validFields++;
            } else if(param.paramid() == "first_name") {
                name = param.sparamval();
                validFields++;
            } else if(param.paramid() == "last_name") {
                surname = param.sparamval();
                validFields++;
            }
        }

        if(validFields == 4) {
            bool paramsOk = true;
            if(username.size() < 2) {
                paramsOk = false;
                resError(res, "Username too short", "tried to register, but provided too short username");
            }

            if(paramsOk && passwd.size() < 7) {
                paramsOk = false;
                resError(res, "Password too short", "tried to register, but provided too short password");
            }

            if(paramsOk && name.size() < 3) {
                paramsOk = false;
                resError(res, "First name too short", "tried to register, but provided too short name");
            }

            if(paramsOk && surname.size() < 3) {
                paramsOk = false;
                resError(res, "Last name too short", "tried to register, but provided too short surname");
            }

            if(paramsOk) {
                UDetails u_d;
                u_d.username = username;
                u_d.name = name;
                u_d.surname = surname;

                bool usernameTaken = false;
                if(!UserManager::getInstance().registerUser(u_d, passwd, usernameTaken)) {
                    if(usernameTaken) {
                        resError(res, "Username already taken", "tried to register, but provided not unique username");
                    } else {
                        resError(res, "Error occured", "tried to register, but internal error occured");
                    }
                } else {
                    res.set_type(ResponseType::OK);
                }
            }
        } else {
            resError(res, "Wrong command format", "tried to register, but command format was wrong");
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::LIST_USER_FILES) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to list user files, but was not logged as admin");
        } else {
            string username, path;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "username") {
                    username = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "path") {
                    path = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !path.empty() && !username.empty()) {
                vector<UFile> files;

                if(!u.listUserFiles(username, path, files)) {
                    resError(res, "Internal error occured", "tried to list user files, but internal error occured");
                } else {
                    for(auto &&file: files) {
                        File *tmp_file = res.add_filelist();
                        tmp_file->set_filename(file.filename);
                        tmp_file->set_filetype(file.type == FILE_REGULAR ? FileType::FILE : FileType::DIRECTORY);
                        tmp_file->set_size(file.size);
                        tmp_file->set_hash(file.hash);
                        tmp_file->set_owner(file.owner_name);
                        tmp_file->set_creationdate(file.creation_date);
                        tmp_file->set_isshared(file.isShared);
                    }

                    res.set_type(ResponseType::FILES);
                }
            } else {
                resError(res, "Wrong command format", "tried to list user files, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::GET_STAT) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to get stats, but was not logged in");
        } else {
            UDetails userDetails;
            if(u.getYourStats(userDetails)) {
                UserDetails* tmp = res.add_userlist();
                tmp->set_firstname(userDetails.name);
                tmp->set_lastname(userDetails.surname);
                tmp->set_role(userDetails.role == USER_ADMIN ? UserRole::ADMIN : UserRole::USER);
                tmp->set_totalspace(userDetails.totalSpace);
                tmp->set_usedspace(userDetails.usedSpace);
                tmp->set_username(userDetails.username);

                res.set_type(ResponseType::STAT);
            } else {
                resError(res, "Error occured", "tried to list users, but error occured");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::DELETE_USER) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to delete user, but was not logged as admin");
        } else {
            if(cmd->params_size() == 1 && cmd->params(0).paramid() == "username" && !cmd->params(0).sparamval().empty()) {
                if(UserManager::getInstance().deleteUser(cmd->params(0).sparamval())) {
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to delete user " + cmd->params(0).sparamval() + ", but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to put data, but command format was wrong");
            }

        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::DELETE) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to get stats, but was not logged in");
        } else {
            if(cmd->params_size() == 1 && cmd->params(0).paramid() == "path" && !cmd->params(0).sparamval().empty()) {

                if(u.deleteFile(cmd->params(0).sparamval())) {
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to delete file " + cmd->params(0).sparamval() + ", but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to delete file, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::DELETE_USER_FILE) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to delete user files, but was not logged as admin");
        } else {
            string username, path;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "username") {
                    username = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "path") {
                    path = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !path.empty() && !username.empty()) {
                u.deleteUserFile(username, path);
            } else {
                resError(res, "Wrong command format", "tried to list user files, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::CHANGE_PASSWD) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to change password, but was not logged in");
        } else {
            string current_passwd, new_passwd;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "current_passwd") {
                    current_passwd = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "new_passwd") {
                    new_passwd = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !current_passwd.empty() && new_passwd.size() > 6) {
                if(u.changePasswd(current_passwd, new_passwd)) {
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to change password, but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to change password, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::CHANGE_USER_PASS) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to change user password, but was not logged as admin");
        } else {
            string username, new_passwd;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "username") {
                    username = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "new_passwd") {
                    new_passwd = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !username.empty() && new_passwd.size() > 6) {
                if(u.changeUserPasswd(username, new_passwd)) {
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to change user password, but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to change user password, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    }  else if (cmd->type() == CommandType::DOWNLOAD) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to download file, but was not logged in");
        } else {
            string filename;
            uint64_t startingChunk = 0;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "file_path") {
                    filename = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "starting_chunk") {
                    startingChunk = (uint64_t) param.iparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !filename.empty()) {
                string data;
                if(u.initFileDownload(filename, startingChunk, data)) {
                    res.set_type(ResponseType::SRV_DATA);
                    res.set_data(data);
                } else {
                    resError(res, "Error occured", "tried to download file " + filename + ", but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to download file, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::C_DOWNLOAD) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to continue downloading file, but was not logged in");
        } else {
            string data;
            if (u.getFileChunk(data)) {
                res.set_type(ResponseType::SRV_DATA);
                res.set_data(data);
            } else {
                resError(res, "Error occured", "tried to continue downloading file, but error occured");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::SHARE) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to share file, but was not logged in");
        } else {
            string username, filename;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "username") {
                    username = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "file_path") {
                    filename = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !username.empty() && !filename.empty()) {
                if(u.shareWith(filename, username)) {
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to share file, but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to share file, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::UNSHARE) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to unshare file, but was not logged in");
        } else {
            string username, filename;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "username") {
                    username = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "file_path") {
                    filename = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !username.empty() && !filename.empty()) {
                if(u.unshareWith(filename, username)) {
                    res.set_type(ResponseType::OK);
                } else {
                    resError(res, "Error occured", "tried to unshare file, but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to unshare file, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::LIST_SHARED) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to list shared files, but was not logged in");
        } else {
            vector <UFile> list;
            if(u.listShared(list)) {
                for(auto&& file: list) {
                    File* tmp_file = res.add_filelist();
                    tmp_file->set_filename(file.filename);
                    tmp_file->set_filetype(file.type == FILE_REGULAR ? FileType::FILE : FileType::DIRECTORY);
                    tmp_file->set_size(file.size);
                    tmp_file->set_hash(file.hash);
                    tmp_file->set_owner(file.owner_name);
                    tmp_file->set_ownerusername(file.owner_username);
                    tmp_file->set_creationdate(file.creation_date);
                    tmp_file->set_isshared(file.isShared);
                }

                res.set_type(ResponseType::FILES);
            } else {
                resError(res, "Error occured", "tried to list shared files, but error occured");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::ADMIN_LIST_SHARED) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to list user shared files, but was not logged as admin");
        } else {
            if(cmd->params_size() == 1 && cmd->params(0).paramid() == "username" && !cmd->params(0).sparamval().empty()) {
                vector <UFile> list;
                if(u.listUserShared(cmd->params(0).sparamval(), list)) {
                    for(auto&& file: list) {
                        File* tmp_file = res.add_filelist();
                        tmp_file->set_filename(file.filename);
                        tmp_file->set_filetype(file.type == FILE_REGULAR ? FileType::FILE : FileType::DIRECTORY);
                        tmp_file->set_size(file.size);
                        tmp_file->set_hash(file.hash);
                        tmp_file->set_owner(file.owner_name);
                        tmp_file->set_ownerusername(file.owner_username);
                        tmp_file->set_creationdate(file.creation_date);
                        tmp_file->set_isshared(file.isShared);
                    }

                    res.set_type(ResponseType::FILES);
                } else {
                    resError(res, "Error occured", "tried to list user shared files, but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to list user shared files, but command format was wrong");
            }

        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::SHARED_DOWNLOAD) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to download shared file, but was not logged in");
        } else {
            string filename, hash, ownerUsername;
            uint64_t startingChunk = 0;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "file_path") {
                    filename = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "starting_chunk") {
                    startingChunk = (uint64_t) param.iparamval();
                    validFields++;
                } else if(param.paramid() == "owner_username") {
                    ownerUsername = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "hash") {
                    hash = param.sparamval();
                    validFields++;
                }
            }

            if(validFields == 4 && !filename.empty()) {
                string data;
                if(u.initSharedFileDownload(filename, ownerUsername, hash, startingChunk, data)) {
                    res.set_type(ResponseType::SRV_DATA);
                    res.set_data(data);
                } else {
                    resError(res, "Error occured", "tried to download shared file " + filename + ", but error occured");
                }
            } else {
                resError(res, "Wrong command format", "tried to download shared file, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::CLEAR_CACHE) {
        if(!(u.isValid() && u.isAuthorized())) {
            resError(res, "You are not logged in", "tried to clear cache, but was not logged in");
        } else {
            if(u.clearCache()) {
                res.set_type(ResponseType::OK);
            } else {
                resError(res, "Error occured", "tried to clear cache, but error occured");
            }
        }

        sendServerResponse(&res);
    } else if (cmd->type() == CommandType::CHANGE_QUOTA) {
        if(!(u.isAdmin())) {
            resError(res, "Not enough permissions", "tried to change user quota, but was not logged as admin");
        } else {
            string username;
            uint64_t newVal = 0;
            uint8_t validFields = 0;

            for(auto& param: cmd->params()) {
                if(param.paramid() == "username") {
                    username = param.sparamval();
                    validFields++;
                } else if(param.paramid() == "new_val") {
                    newVal = param.iparamval();
                    validFields++;
                }
            }

            if(validFields == 2 && !username.empty()) {
                if(!u.changeUserTotalStorage(username, newVal)) {
                    resError(res, "Internal error occured", "tried to change user quota, but internal error occured");
                } else {
                    res.set_type(ResponseType::OK);
                }
            } else {
                resError(res, "Wrong command format", "tried to change user quota, but command format was wrong");
            }
        }

        sendServerResponse(&res);
    }
}
