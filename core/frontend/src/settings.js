module.exports = {
  TokenKey: 'Authorization',
  TokenExpKey: 'de-login-login-exp',
  RefreshTokenKey: 'refreshauthorization',
  LinkTokenKey: 'LINK-PWD-TOKEN',
  title: '爱世达BI工具',
  /* for sso */
  IdTokenKey: 'IdToken',
  AccessTokenKey: 'AccessToken',
  CASSESSION: 'JSESSIONID',
  DownErrorKey: 'de-down-error-msg',

  /**
   * @type {boolean} true | false
   * @description Whether fix the header
   */
  //   fixedHeader: false,

  /**
   * @type {boolean} true | false
   * @description Whether show the logo in sidebar
   */
  sidebarLogo: false,
  showSettings: true,
  interruptTokenContinueUrls: [
    '/api/sys_msg/list/',
    '/dataset/taskLog/list/'
  ]
}
