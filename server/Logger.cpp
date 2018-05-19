//
// Created by milosz on 19.05.18.
//

#include "Logger.h"

using namespace std;

void Logger::print_msg() {
    while(true) {
        unique_lock<mutex> l(queue_mutex);
        if (msg_queue.empty()) {
            queue_empty.wait(l);
        }

        bool printed = false;

        if(!msg_queue.empty()) {
            Msg msg = msg_queue.front();
            msg_queue.pop();

            string message;

            char time_buf[12];
            tm *t = localtime(&msg.time);
            strftime(time_buf, 12, "[%H:%M:%S]", t);
            string time(time_buf);

            message = time;

            if (msg.level == DEBUG) {
                message += "[DEBUG]";
            }
            if (msg.level == INFO) {
                message += "[INFO]";
            }
            if (msg.level == WARN) {
                message += "[WARN]";
            }
            if (msg.level == ERR) {
                message += "[ERROR]";
            }

            message += "[" + msg.author + "] ";

            message += msg.body;

            cout << "\r" << message << endl;

            printed = true;

            if (msg.body == "bye" && *should_exit) {
                break;
            }
        }

        if(input != nullptr) {
            if(*input != last_printed) {
                printed = true;
            }

            if(printed) {
                last_printed = *input;
                cout<<"\r> "<<last_printed<<" \b"<<flush;
            }
        }
    }
}

Logger::Logger(bool* s_e) {
    should_exit = s_e;

    printer = thread(&Logger::print_msg, this);
}

Logger::~Logger() {
    printer.join();
}

void Logger::notify() {
    queue_empty.notify_one();
}

void Logger::set_input_string(string* in) {
    input = in;
}

void Logger::log(const string& author, const string& body) {
    add_message(DEBUG, author, body);
}

void Logger::info(const string& author, const string& body) {
    add_message(INFO, author, body);
}

void Logger::warn(const string& author, const string& body) {
    add_message(WARN, author, body);
}

void Logger::err(const string& author, const string& body) {
    add_message(ERR, author, body);
}

void Logger::add_message(MessageLevel lvl, const string& author, const string& body) {
    Msg msg;
    msg.level = lvl;
    msg.author = author;
    msg.body = body;
    time(&msg.time);
    queue_mutex.lock();
    msg_queue.push(msg);
    queue_mutex.unlock();
    queue_empty.notify_one();
}
