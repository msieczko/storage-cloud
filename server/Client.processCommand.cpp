#include "Client.h"

using namespace std;
using namespace StorageCloud;

bool Client::processCommand(Command* cmd) {
    logger->log(id, "Received command '" + CommandType_Name(cmd->type()) + "' (" + to_string(cmd->type()) +
                    "), with " + to_string(cmd->params_size()) + " params");

    ServerResponse res;

    if(cmd->type() == CommandType::LOGIN) {
        bool params_ok = (cmd->params_size() == 2);
        bool alreadyAuthorized = u.isAuthorized();
        string sid;
        string passwd, username;

        if(params_ok && !alreadyAuthorized) {
            sessionId = "";
            if(cmd->params(0).paramid() == "username" && cmd->params(1).paramid() == "password") {
                passwd = cmd->params(1).sparamval();
                username = cmd->params(0).sparamval();
            } else if (cmd->params(0).paramid() == "password" && cmd->params(1).paramid() == "username") {
                passwd = cmd->params(0).sparamval();
                username = cmd->params(1).sparamval();
            } else {
                params_ok = false;
            }

            if(params_ok) {
                if(u.addUsername(username)) {
                    u.loginByPassword(passwd, sid);
                }
            }
        }

        if(params_ok && u.isAuthorized()) {
            sessionId = sid;
            res.set_type(ResponseType::LOGGED);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("sid");
            tmp_param->set_bparamval(sid);
            logger->log(id, "user " + username + " logged in");
        } else {
            res.set_type(ResponseType::ERROR);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("msg");

            if(params_ok) {
                tmp_param->set_sparamval("Invalid username/password");
                if(!u.isValid()) {
                    logger->log(id, "client " + username + " tried to log in, but that user doesn't exist");
                } else if(!u.isAuthorized()) {
                    logger->log(id, "client " + username + " tried to log in, but provided wrong password");
                } else {
                    logger->warn(id, "client " + username + " tried to log in, but internal error occurred");
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
        string sid, username;

        if(params_ok && !alreadyAuthorized) {
            sessionId = "";
            if(cmd->params(0).paramid() == "username" && cmd->params(1).paramid() == "sid") {
                sid = cmd->params(1).bparamval();
                username = cmd->params(0).sparamval();
            } else if (cmd->params(0).paramid() == "sid" && cmd->params(1).paramid() == "username") {
                sid = cmd->params(0).bparamval();
                username = cmd->params(1).sparamval();
            } else {
                params_ok = false;
            }

            if(params_ok) {
                if(u.addUsername(username)) {
                    u.loginBySid(sid);
                }
            }
        }

        if(params_ok && u.isAuthorized()) {
            sessionId = sid;
            res.set_type(ResponseType::LOGGED);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("sid");
            tmp_param->set_bparamval(sid);
            logger->log(id, "user " + username + " relogged in");
        } else {
            res.set_type(ResponseType::ERROR);
            Param* tmp_param = res.add_params();
            tmp_param->set_paramid("msg");

            if(params_ok) {
                tmp_param->set_sparamval("Invalid username or session ID");
                if(!u.isValid()) {
                    logger->log(id, "client " + username + " tried to relogin, but that user doesn't exist");
                } else if(!u.isAuthorized()) {
                    logger->log(id, "client " + username + " tried to relogin, but provided wrong sid");
                } else {
                    logger->warn(id, "client " + username + " tried to relogin, but internal error occurred");
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
        }

        sendServerResponse(&res);
    }
}