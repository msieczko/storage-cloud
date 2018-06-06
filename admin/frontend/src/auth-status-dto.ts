import { UserDto } from './users/dto/user-dto';

export interface AuthStatusDto {
  authenticated: boolean;
  current: UserDto;

}