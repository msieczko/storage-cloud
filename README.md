### Protokół

Funkcja | Komenda klienta | Komenda admina | Odpowiedź serwera
--- | --- | --- | ---
Logowanie | LOGIN username pass | - | LOGGED sid [warn_list]
Logowanie | RELOGIN sid | - | LOGGED sid [warn_list]
Wylogowanie | LOGOUT | - | OK / ERROR code msg
Stworzenie użytkownika | REGISTER username pass | - | OK / ERROR code msg
Usunięcie użytkownika | - | DELETE_USER username | OK / ERROR code msg
Zmiana hasła | CHANGE_PASSWD current_passwd new_passwd | CHANGE_USER_PASS username new_pass | OK / ERROR code msg
Wyświetlenie zużycia przydzielonego miejsca | GET_STAT | USER_STAT username | STAT [stat_list] - do przemyślenia
Przeglądanie katalogów i plików | LIST_FILES path | LIST_USER_FILES username path | FILES [file_with_metadata_list]
Stworzenie katalogu | MKDIR path | - | OK / ERROR code msg
Skasowanie katalogu lub pliku | DELETE path | DELETE_USER_FILE username path | OK / ERROR code msg
Zmiana nazwy katalogu lub pliku | RENAME old_path new_path | - | OK / ERROR code msg
Udostępnienie pliku | SHARE file_path target_usernames_list | - | OK / ERROR code msg
Anulowanie dostępu do pliku | UNSHARE file_path [target_usernames_list] | ADMIN_UNSHARE owner_username file_path [target_usernames_list] | OK / ERROR code msg
Wyświetlenie info o dostępie do pliku | SHARE_INFO file_path | ADMIN_SHARE_INFO owner_username file_path | SHARED file_path [who_list]
Wysłanie ostrzeżenia | - | WARN user message | OK / ERROR code msg
Przeniesienie pliku lub katalogu | MOVE old_path new_path | - | OK / ERROR code msg
Pobieranie pliku | DOWNLOAD file_path | - | SRV_DATA data
Zainicjalizowanie wgrywania pliku | METADATA target_file_path size file_checksum | - | CAN_SEND starting_chunk / ERROR code msg
Wgrywanie danych | USR_DATA data | - | OK / ERROR code msg