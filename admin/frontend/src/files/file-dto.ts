export interface FileDTO {
  filename: string;
  fileType: FileType;
  size: number;
  owner: string;
  ownerUsername: string;
  creationDate: Date;
  isShared: boolean;
}

export enum FileType {
  FILE = 'FILE', DIRECTORY = 'DIRECTORY'
}