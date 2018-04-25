### Protokół

Funkcja | Komenda klienta | Komenda admina | Odpowiedź serwera
--- | --- | --- | ---
Logowanie | LOGIN username pass | <- | LOGGED sid warn_count [warn_list]
Wylogowanie | LOGOUT sid | <- | OK / ERROR code msg
Stworzenie użytkownika | REGISTER username pass | REGISTER sid username pass | OK / ERROR code msg
Usunięcie użytkownika | - | DELETE_USER sid username | OK / ERROR code msg
Zmiana hasła użytkownika | - | CHANGE_USER_PASS sid username new_pass | OK / ERROR code msg
Wyświetlenie zużycia przydzielonego miejsca | STAT sid | USER_STAT sid username | STAT stat_count [stat_list] - do przemyślenia
Przeglądanie katalogów i plików | LIST_FILES sid path | LIST_USER_FILES sid username path | FILES count [file_with_metadata_list]
Stworzenie katalogu | MKDIR sid path | - | OK / ERROR code msg
Skasowanie katalogu lub pliku | DELETE sid path | DELETE_USER_FILE sid username path | OK / ERROR code msg
Zmiana nazwy katalogu lub pliku | RENAME sid old_path new_path | - | OK / ERROR code msg
Udostępnienie pliku | SHARE sid file_path target_usernames_list | - | OK / ERROR code msg
Anulowanie dostępu do pliku | UNSHARE sid file_path [target_usernames_list] | UNSHARE sid owner_username file_path [target_usernames_list] | OK / ERROR code msg
Wyświetlenie info o dostępie do pliku | SHARE_INFO sid file_path | SHARE_INFO sid owner_username file_path | SHARED file_path who_count [who_list]
Wysłanie ostrzeżenia | - | WARN sid user message | OK / ERROR code msg
Przeniesienie pliku lub katalogu | MOVE sid old_path new_path | - | OK / ERROR code msg
Pobieranie pliku | DOWNLOAD sid file_path | - | DATA data
Zainicjalizowanie wgrywania pliku | METADATA sid target_file_path size file_checksum | - | CAN_SEND starting_chunk / ERROR code msg
Wgrywanie danych | DATA sid data | - | OK / ERROR code msg