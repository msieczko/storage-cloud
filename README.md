### Protokół

Funkcja | Komenda klienta | Komenda admina | Odpowiedź serwera
--- | --- | --- | ---
Logowanie | LOGIN username(string) password(string) | - | LOGGED sid(bytes) [warn_list] / ERROR msg(str)
Logowanie | RELOGIN sid(bytes) username(string) | - | LOGGED sid(bytes) [warn_list] / ERROR msg(str)
Wylogowanie | LOGOUT | - | OK / ERROR code msg
Stworzenie użytkownika | REGISTER username pass first_name last_name | - | OK / ERROR code msg
Lista użytkowników | - | LIST_USERS | USERS [User_message_list]
Usunięcie użytkownika | - | DELETE_USER username | OK / ERROR code msg
Zmiana hasła | CHANGE_PASSWD current_passwd new_passwd | CHANGE_USER_PASS username new_pass | OK / ERROR code msg
Wyświetlenie zużycia przydzielonego miejsca | GET_STAT | - | STAT [User_message_list]
Przeglądanie katalogów i plików | LIST_FILES path | LIST_USER_FILES username path | FILES [File_message_list] / ERROR msg
Stworzenie katalogu | MKDIR path | - | OK / ERROR code msg
Skasowanie katalogu lub pliku | DELETE path | DELETE_USER_FILE username path | OK / ERROR code msg
Udostępnienie pliku | SHARE file_path username | - | OK / ERROR code msg
Anulowanie dostępu do pliku | UNSHARE file_path username | ADMIN_UNSHARE owner_username file_path username | OK / ERROR code msg
Wyświetlenie info o dostępie do pliku | SHARE_INFO file_path | ADMIN_SHARE_INFO owner_username file_path | SHARED [list_with_usernames]
Wysłanie ostrzeżenia | - | WARN user message | OK / ERROR code msg
Zainicjalizowanie pobierania swojego pliku | DOWNLOAD file_path starting_chunk | - | SRV_DATA data / ERROR msg
Zainicjalizowanie pobierania czyjegoś pliku | SHARED_DOWNLOAD filename starting_chunk owner_username hash | - | SRV_DATA data / ERROR msg
Prośba o kolejny fragment pliku | C_DOWNLOAD | - | SRV_DATA data / ERROR msg
Zainicjalizowanie wgrywania pliku | METADATA target_file_path size file_checksum | - | CAN_SEND starting_chunk / ERROR code msg
Wgrywanie danych | USR_DATA data | - | OK / ERROR code msg
Usunięcie nie do końca przesłanych plików (zwróci error także jeśli cache był pusty) | CLEAR_CACHE | - | OK / ERROR msg
Zmiana dostępnego miejsca | - | CHANGE_QUOTA username(string) new_val(int) | OK / ERROR msg
Wylistowanie plików udostępnionych dla użytkownika | LIST_SHARED | ADMIN_LIST_SHARED username | FILES [File_message_list] / ERROR msg