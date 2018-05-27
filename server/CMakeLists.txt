cmake_minimum_required(VERSION 3.0)
project(server)

find_package(libmongocxx REQUIRED)

set(CMAKE_CXX_STANDARD 11)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

add_executable(server protbuf/messages.pb.cc main.cpp main.h utils.h utils.cpp Client.cpp Client.h Logger.cpp Logger.h Database.cpp Database.h User.cpp User.h Client.processCommand.cpp)

target_include_directories(server PRIVATE ${LIBMONGOCXX_INCLUDE_DIRS})
target_link_libraries(server -pthread -I/usr/local/include -L/usr/local/lib -lprotobuf -pthread -lpthread -lcrypto ${LIBMONGOCXX_LIBRARIES})
target_compile_definitions(server PRIVATE ${LIBMONGOCXX_DEFINITIONS})

add_executable(client protbuf/messages.pb.cc sock_client1.cpp main.h utils.h utils.cpp)

target_link_libraries(client -pthread -I/usr/local/include -L/usr/local/lib -lprotobuf -pthread -lpthread -lcrypto)