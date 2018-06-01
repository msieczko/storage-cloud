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
                u.listFilesinPath(cmd->params(0).sparamval(), files);

                for(auto&& file: files) {
//                    logger.info("FILES", file.filename + " " + file.owner_name);
                    File* tmp_file = res.add_filelist();
                    tmp_file->set_filename(file.filename);
                    tmp_file->set_filetype(file.type == FILE_REGULAR ? FileType::FILE : FileType::DIRECTORY);
                    tmp_file->set_size(file.size);
                    tmp_file->set_hash(file.hash);
                    tmp_file->set_owner(file.owner_name);
                    tmp_file->set_creationdate(file.creation_date);
                }

                res.set_type(ResponseType::FILES);
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
                if(path.size() > 0 && hash.size() == FILE_HASH_SIZE && size < 1024*1024*1024*50) {
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
                    res.set_type(ResponseType::OK);
                } else {
                    if(wyn == ADD_FILE_INTERNAL_ERROR) {
                        resError(res, "Internal error occured", "tried to add metadata, but internal error occured");
                    } else if(wyn == ADD_FILE_WRONG_DIR) {
                        resError(res, "Wrong path", "tried to add metadata, but provided wrong path");
                    } else if(wyn == ADD_FILE_EMPTY_NAME) {
                        resError(res, "Filename empty", "tried to add metadata, but provided empty filename");
                    } else if(wyn == ADD_FILE_FILE_EXISTS) {
                        resError(res, "Directory already exists", "tried to add metadata, but directory already exists");
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
    }
}