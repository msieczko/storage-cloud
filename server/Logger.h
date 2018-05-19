#ifndef SERVER_LOGGER_H
#define SERVER_LOGGER_H

#include "main.h"

enum MessageLevel {
    DEBUG,
    INFO,
    WARN,
    ERR,
};

class Logger {
private:
    struct Msg {
        MessageLevel level;
        std::string body;
        std::string author;
        time_t time;
    };
    std::queue<Msg> msg_queue;
    std::mutex queue_mutex;
    std::condition_variable queue_empty;
    std::string* input = nullptr;
    std::string last_printed = "";
    std::thread printer;
    bool* should_exit;

    void print_msg();

public:
    Logger(bool* s_e);

    ~Logger();

    void notify();

    void set_input_string(std::string*);

    void log(const std::string&, const std::string&);

    void info(const std::string&, const std::string&);

    void warn(const std::string&, const std::string&);

    void err(const std::string&, const std::string&);

    void add_message(MessageLevel, const std::string&, const std::string&);
};


#endif //SERVER_LOGGER_H
