import * as React from 'react';
import { translate } from 'react-i18next';
import { FileDTO, FileType } from './file-dto';
import { ApplicationProps } from '../App';
import withAppHoc, { WithAppProps } from '../common/app-hoc';
import { HttpService } from '../common/http-service';
import { UserDto } from '../users/dto/user-dto';
import { Utils } from '../common/utils';

interface FilesListState {
  fetchedFiles: FileDTO[];
  filteredFiles: FileDTO[];
  errorInfo: string;
  fetchInProgress: boolean;
  filters: {
    filenameFilter: string,
  };
}

interface FilesListProps {
  chosenUser: UserDto | null;
  chosenPath: String[];
  selectPath: (path: String[], callback: Function) => void;
}

class FilesList extends React.Component<ApplicationProps & WithAppProps & FilesListProps, FilesListState> {
  private httpService: HttpService;

  constructor(props: ApplicationProps & WithAppProps & FilesListProps) {
    super(props);
    this.httpService = props.httpService;

    this.state = {
      fetchedFiles: [],
      filteredFiles: [],
      errorInfo: '',
      fetchInProgress: false,
      filters: {
        filenameFilter: ''
      }
    };

    this.handleFilterValueChange = this.handleFilterValueChange.bind(this);
    this.fetchList = this.fetchList.bind(this);
    this.deleteFile = this.deleteFile.bind(this);
  }

  fetchList(props?: { chosenUser: UserDto | null, chosenPath: String[] }) {
    this.setState({fetchInProgress: true});
    if (!props) {
      props = this.props;
    }
    if (props.chosenUser) {
      let chosenPath = '';
      if (props.chosenPath.length > 0) {
        chosenPath = props.chosenPath.join('/');
      }
      console.log('fetchList():  api/files/' + props.chosenUser.username + '/' + chosenPath);
      this.httpService.get('api/files/' + props.chosenUser.username + '/' + chosenPath)
        .then(response => {
            if (response.ok) {
              response.json()
                .then(data => this.setState({
                  filteredFiles: data,
                  fetchedFiles: data,
                  fetchInProgress: false
                })).catch(this.setErrorInfo());
            }
          }
        ).catch(this.setErrorInfo());
    }
  }

  deleteFile(file: FileDTO) {
    let chosenPath = '';
    if (this.props.chosenPath.length > 0) {
      chosenPath = this.props.chosenPath.join('/') + '/';
    }
    chosenPath += file.filename;
    if (this.props.chosenUser) {
      console.log('fetchList():  api/delete/' + this.props.chosenUser.username + '/' + chosenPath);
      this.httpService.get('api/delete/' + this.props.chosenUser.username + '/' + chosenPath)
        .then(response => {
            if (response.ok) {
              response.text()
                .then(text => {
                  if (text === 'OK') {
                    this.fetchList();
                  } else {
                    this.setState({errorInfo: text});
                  }
                }).catch(this.setErrorInfo());
            } else {
              this.setState({fetchInProgress: false});
            }
          }
        ).catch(this.setErrorInfo());
    }
  }

  componentWillReceiveProps(nextProps: Readonly<ApplicationProps & WithAppProps & FilesListProps>,
                            nextContext: any): void {
    console.log('componentWillReceiveProps');
    this.fetchList(nextProps);
  }

  componentDidMount() {
    console.log('files-list componentDidMount()');
    this.fetchList();
  }

  setErrorInfo() {
    return (message: Response) => {
      this.setState({errorInfo: message.toString(), fetchInProgress: false});
    };
  }

  refreshFilter() {
    let filteredFiles = this.state.fetchedFiles.filter(value => {
      let show: boolean = true;
      if (this.state.filters.filenameFilter !== '') {
        show = value.filename.toLowerCase().indexOf(this.state.filters.filenameFilter.toLowerCase()) >= 0;
      }
      return show;
    });

    this.setState({filteredFiles: filteredFiles});
  }

  handleFilterValueChange(e: React.ChangeEvent<HTMLInputElement>) {
    const {name, value} = e.target;

    this.setState(
      {filters: {...this.state.filters, [name]: value}},
      () => {
        this.refreshFilter();
      });
  }

  fileRowOnClick(props: { file: FileDTO }) {
    if (props.file.fileType === FileType.DIRECTORY.valueOf()) {
      console.log('selectPath');
      this.props.selectPath(
        this.props.chosenPath.concat(props.file.filename),
        () => this.fetchList()
      );
    }
  }

  render() {
    const {t} = this.props;
    console.log('render: ' + this.props.chosenPath.join('/'));
    const FileRow = (props: { file: FileDTO }) => {
      return (
        <tr className="clickable-row py-0 my-0">
          <td onClick={() => this.fileRowOnClick(props)}>
            {props.file.filename + (props.file.fileType === FileType.DIRECTORY.valueOf() ? '/' : '')}
          </td>
          <td onClick={() => this.fileRowOnClick(props)}>
            {new Date((props.file.creationDate)).toISOString().substr(0, 10)}
          </td>
          <td onClick={() => this.fileRowOnClick(props)}>
            {props.file.fileType === FileType.FILE.valueOf() && Utils.humanFileSize(props.file.size, true)}
          </td>
          <td>
            <button
              type="button"
              className="btn btn-sm my-0 btn-outline-danger"
              onClick={e => this.deleteFile(props.file)}
            >
              {t('FILES.DELETE')}
            </button>
          </td>
        </tr>
      );
    };

    function Files(props: { file: FileDTO[] }) {
      return (
        <tbody>
        {props.file.map((f) =>
          <FileRow key={f.filename} file={f}/>
        )}
        </tbody>
      );
    }

    return (
      <div>
        <div className="card row">
          <div className="card-body row">

            <div className="col">
              <div className="row">
                <h6 className="col-auto card-subtitle mt-2">
                  {t('COMMONS.FILTER')}
                </h6>
                <input
                  type="text"
                  name="filenameFilter"
                  className="col-3 form-control mr-3"
                  value={this.state.filters.filenameFilter}
                  onChange={this.handleFilterValueChange}
                  placeholder={t('FILES.FILTER.FILENAME')}
                />
              </div>
            </div>

          </div>
        </div>
        {
          this.state.errorInfo !== '' &&
          <div className="row my-1 alert alert-danger" role="alert">
            {this.state.errorInfo}
          </div>
        }
        {this.props.chosenUser ?
          <div className="row">
            <table className="table table-hover table-striped table-bordered text-left">
              <thead className="thead-light">
              <tr>
                <th>{t('FILES.FILE_ROW.FILENAME')}</th>
                <th>{t('FILES.FILE_ROW.CREATION_DATE')}</th>
                <th>{t('FILES.FILE_ROW.SIZE')}</th>
                <th>{t('FILES.FILE_ROW.ACTIONS')}</th>
              </tr>
              </thead>
              <Files file={this.state.filteredFiles}/>
            </table>
          </div>
          :
          <div className="row my-1 alert alert-primary" role="alert">
            {t('FILES.MESSAGE.CHOOSE_USER_FIRST')}
          </div>
        }
        {
          this.props.chosenUser && !this.state.fetchInProgress && this.state.fetchedFiles.length === 0 &&
          <div className="row my-0 alert alert-primary" role="alert">
            {t('FILES.MESSAGE.EMPTY_DIRECTORY')}
          </div>
        }
      </div>
    );
  }
}

export default translate()(withAppHoc(FilesList));