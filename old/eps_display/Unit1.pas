unit Unit1;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms, Dialogs, StdCtrls, ExtCtrls, DB, ADODB, Grids, DBGrids, DBCtrls, Mask, DateUtils, ShellAPI, DBTables, StrUtils, Math,
  Sockets, ScktComp , IdHTTP;

type
  TForm1 = class(TForm)
    selectQry: TADOQuery;
    mainQry: TADOQuery;
    operatorQry: TADOQuery;
    ds: TDataSource;
    refreshTimer: TTimer;
    DBConnection: TADOConnection;
    Panel1: TPanel;
    operatorNameLb: TLabel;
    operatorLb: TLabel;
    lineCb: TComboBox;
    workOrderLb: TLabel;
    workOrderCb: TComboBox;
    board_tybeLb: TLabel;
    boardTypeCb: TComboBox;
    materialNoLb: TLabel;
    scanMaterialNoLb: TLabel;
    scanMaterialNoNameLb: TLabel;
    materialNoNameLb: TLabel;
    lineseatLb: TLabel;
    scanLineseatLb: TLabel;
    scanLineSeatNameLb: TLabel;
    lineSeatNameLb: TLabel;
    Panel2: TPanel;
    dataGrid: TDBGrid;
    typeLb: TLabel;
    resultLb: TLabel;
    lineLb: TLabel;
    updateQry: TADOQuery;
    secondaryQry: TADOQuery;
    Button1: TButton;
    resetQry: TADOQuery;
    procedure dataGridDrawColumnCell(Sender: TObject; const Rect: TRect; DataCol: Integer; Column: TColumn; State: TGridDrawState);
    procedure lineCbChange(Sender: TObject);
    procedure workOrderCbChange(Sender: TObject);
    procedure boardTypeCbChange(Sender: TObject);
    procedure refreshTimerTimer(Sender: TObject);
    procedure updateTop();
    procedure lineCbDropDown(Sender: TObject);
    procedure workOrderCbDropDown(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure changeIcon(colnum: Integer; Sender: TObject; const Rect: TRect);
    function showResult(s : String):String;
    function getBoardTypeNo(Name:string) : string;
    procedure Button1Click(Sender: TObject);
    procedure requestHttp(action:string; isClose:Boolean);
    procedure FormDestroy(Sender: TObject);

  private

    { Private declarations }
    procedure OnMouseWheel(var Msg: TMsg; var Handled: Boolean);
  public
    { Public declarations }
  end;

var
  Form1: TForm1;
  executing: Boolean;
  num: Real;
  board_type: Real;

implementation

{$R *.dfm}

//�������ݼ���ʾ����
procedure TForm1.dataGridDrawColumnCell(Sender: TObject; const Rect: TRect; DataCol: Integer; Column: TColumn; State: TGridDrawState);
begin
  if (Column.FieldName = 'store_issue_result') then
  begin
    changeIcon(1,Sender,Rect);
  end
  else if (Column.FieldName = 'feed_result') then
  begin
    changeIcon(2,Sender,Rect);
  end
  else if (Column.FieldName = 'change_result') then
  begin
   changeIcon(3,Sender,Rect);
  end
  else if (Column.FieldName = 'check_result') then
  begin
   changeIcon(4,Sender,Rect);
  end
  else if (Column.FieldName = 'check_all_result') then
  begin
   changeIcon(5,Sender,Rect);
  end
  else if (Column.FieldName = 'first_check_all_result') then
  begin
    changeIcon(6,Sender,Rect);
  end;
end;


procedure TForm1.changeIcon(colnum: Integer; Sender: TObject; const Rect: TRect);
begin
  TDBGrid(Sender).Canvas.FillRect(Rect);
  if (TDBGrid(Sender).Fields[colnum].Value = 0) then
  begin
    dataGrid.Canvas.Font.Color := clRed;
    TDBGrid(Sender).Canvas.TextRect(Rect, Rect.Left + 2, Rect.Top + 2, '��');
  end
  else if (TDBGrid(Sender).Fields[colnum].Value = 1) then
  begin
    dataGrid.Canvas.Font.Color := clGreen;
    TDBGrid(Sender).Canvas.TextRect(Rect, Rect.Left + 2, Rect.Top + 2, '��');
  end
  else if (TDBGrid(Sender).Fields[colnum].Value = 2) then
  begin
    dataGrid.Canvas.Font.Color := clBlue;
    TDBGrid(Sender).Canvas.TextRect(Rect, Rect.Left + 2, Rect.Top + 2, '��');
  end
  else if (TDBGrid(Sender).Fields[colnum].Value = 3) then
  begin
    dataGrid.Canvas.Font.Color := TColor($000075ff);
    TDBGrid(Sender).Canvas.TextRect(Rect, Rect.Left + 2, Rect.Top + 2, '��');
  end
  else if (TDBGrid(Sender).Fields[colnum].Value = 4) then
  begin
    dataGrid.Canvas.Font.Color := clPurple;
    TDBGrid(Sender).Canvas.TextRect(Rect, Rect.Left + 2, Rect.Top + 2, '��');
  end
end;



//ˢ������
procedure TForm1.updateTop();
var
  strsql: string;
  w1:string;
  w2:string;
  w3:string;
  typename: string;
  temp: string;
  board_type: string;
begin
  executing := True;
  mainQry.SQL.Clear;
  operatorQry.SQL.Clear;
  board_type := getBoardTypeNo(boardTypeCb.Text);
  //��ѯprogram_item_visit��
  strsql := '(SELECT program_item_visit.* FROM program_item_visit INNER JOIN program ON program.`id`=program_item_visit.`program_id` WHERE line=''' + lineCb.Text + ''' AND work_order=''' + workOrderCb.Text + ''' AND board_type="' + board_type + '" AND';
  w1 := '(store_issue_result = 0 OR feed_result = 0 OR change_result = 0 OR (check_all_result % 3) = 0 OR (check_result % 3) = 0 OR first_check_all_result = 0))';
  w2 := '(change_result = 4))';
  w3 := '(store_issue_result != 0 OR feed_result != 0 OR (change_result % 4) !=0 OR (check_all_result % 3) != 0 OR (check_result % 3) != 0 OR first_check_all_result != 0))';
  mainQry.SQL.Add(strsql + w1 + 'union' + strsql + w2 + 'union' + strsql + w3);
  mainQry.Open;
  mainQry.First;

  //��䶥����������
  scanLineseatLb.Caption := mainQry.fieldbyname('scan_lineseat').AsString;
  lineseatLb.Caption := mainQry.fieldbyname('lineseat').AsString;
  materialNoLb.Caption := mainQry.fieldbyname('material_no').AsString;
  scanMaterialNoLb.Caption := mainQry.fieldbyname('scan_material_no').AsString;
  typename := mainQry.fieldbyname('last_operation_type').AsString;
  //������Ͻǽ��
  if (typename = '0') then
  begin
    typeLb.Caption := '����';
    temp := showResult(mainQry.fieldbyname('feed_result').AsString);
  end
  else if (typename = '1') then
  begin
    typeLb.Caption := '����';

    temp := showResult(mainQry.fieldbyname('change_result').AsString);
  end
  else if (typename = '2') then
  begin
    typeLb.Caption := '����';
    temp := showResult(mainQry.fieldbyname('check_result').AsString);
  end
  else if (typename = '3') then
  begin
    typeLb.Caption := 'ȫ��';
    temp := showResult(mainQry.fieldbyname('check_all_result').AsString);
  end
  else if (typename = '4') then
  begin
    typeLb.Caption := '����';
    temp := showResult(mainQry.fieldbyname('store_issue_result').AsString);
  end
  else if (typename = '5') then
  begin
    typeLb.Caption := '�׼�';
    temp := showResult(mainQry.fieldbyname('first_check_all_result').AsString);
  end
  else
  begin
     temp := showResult('2')
  end;

  resultLb.Caption := temp;
  mainQry.Active := True;
  //��д����Ա
  operatorQry.SQL.Add('SELECT operation.operator');
  operatorQry.SQL.Add('FROM program_item_visit INNER JOIN operation ON');
  operatorQry.SQL.Add('(operation.material_no=program_item_visit.material_no AND program_item_visit.`program_id`=operation.`program_id` AND program_item_visit.`lineseat`=operation.`lineseat`)');
  operatorQry.SQL.Add('WHERE operation.line=''' + lineCb.Text + ''' AND operation.work_order=''' + workOrderCb.Text + ''' AND operation.board_type=''' + board_type + '''');
  operatorQry.SQL.Add('ORDER BY operation.time DESC LIMIT 1');
  operatorQry.Open;
  operatorLb.Caption := operatorQry.fieldbyname('operator').AsString;
  executing := False;
end;


function TForm1.showResult(s : String):String;
begin
   if (s = '0') then
   begin
     typeLb.Color := clRed;
     resultLb.Color := clRed;
     showResult:='FAIL';

   end
   else if(s = '1')then
   begin
     typeLb.Color := clGreen;
     resultLb.Color := clGreen;
     showResult:='PASS';

   end
   else if(s = '2') then
   begin
     typeLb.Caption:='����';
     typeLb.Color := clGreen;
     resultLb.Color := clGreen;
     showResult:='���';
   end
   else if(s = '3') then
   begin
     typeLb.Color := clRed;
     resultLb.Color := clRed;
     showResult:='�ѳ�ʱ';
   end
   else if(s = '4') then
   begin
     typeLb.Caption:='�ѻ���';
     typeLb.Color := clPurple;
     resultLb.Color := clPurple;
     showResult:='�����';
   end;

end;

//ѡ���ߺź󣬲�ѯ����Ӧ����
procedure TForm1.lineCbChange(Sender: TObject);
var
  strsql: string;
  currW : string;
begin
  selectQry.SQL.Clear;
  workOrderCb.items.Clear;
  boardTypeCb.items.Clear;
  strsql := 'select distinct work_order from program where line=''' + lineCb.Text + ''' and state=1';
  selectQry.sql.add(strsql);
  selectQry.Active := True;
  selectQry.First;
  while not selectQry.eof do
  begin
    currW := selectQry.FieldByName('work_order').asstring;
    workOrderCb.items.add(currW);
    selectQry.next;
  end;

end;

//ѡ�񹤵��󣬲�ѯ����Ӧ��������
procedure TForm1.workOrderCbChange(Sender: TObject);
var
  strsql: string;
  board_type: string;
begin
  boardTypeCb.items.Clear;
  selectQry.SQL.Clear;
  strsql := ' select board_type from program where work_order=''' + workOrderCb.Text + ''' and line = ''' + lineCb.Text +''' and state=1';
  selectQry.sql.add(strsql);
  selectQry.Active := True;
  while not selectQry.eof do
  begin
    board_type := selectQry.fieldByName('board_type').AsString;
    if (board_type = '0') then
    begin
      boardTypeCb.Items.Add('Ĭ��');
    end
    else if (board_type = '1') then
    begin
      boardTypeCb.Items.Add('AB��');
    end
    else if (board_type = '2') then
    begin
      boardTypeCb.Items.Add('A��');
    end
    else if (board_type = '3') then
    begin
      boardTypeCb.Items.Add('B��');
    end;
    selectQry.next;

  end;

end;



//ѡ��������ͺ󣬸������ݣ�����ʾ���
procedure TForm1.boardTypeCbChange(Sender: TObject);
begin
  requestHttp('switch',false);
  updateTop;
  dataGrid.Show;
end;

//�������
procedure TForm1.FormCreate(Sender: TObject);
begin
  executing := False;
  operatorQry.Active := False;
  dataGrid.Hide;
  Application.OnMessage := OnMouseWheel;
  Form1.WindowState := wsMaximized;
end;


//����ɹ���
procedure TForm1.OnMouseWheel(var Msg: TMsg; var Handled: Boolean);
begin
  try
    if (Msg.message = WM_MouseWheel) and ((Screen.ActiveForm.ActiveControl.ClassName = 'TDBGrid') or (Screen.ActiveForm.ActiveControl.ClassName = 'TDBGridInplaceEdit')) then
    begin
      if Msg.wParam > 0 then
        SendMessage(Screen.ActiveForm.ActiveControl.Handle, WM_VSCROLL, SB_PAGEUP, 0)
      else
        SendMessage(Screen.ActiveForm.ActiveControl.Handle, WM_VSCROLL, SB_PAGEDOWN, 0);
      Handled := True;
    end;
  except
  end;
end;

//ˢ�����ݶ�ʱ���¼�
procedure TForm1.refreshTimerTimer(Sender: TObject);
begin
  if (boardTypeCb.Text <> '') and (workOrderCb.Text <> '') and (executing = FALSE) then
  begin
    //ˢ��
    updateTop;
    dataGrid.DataSource.DataSet.Active := FALSE;
    dataGrid.DataSource.DataSet.Active := TRUE;
  end;
end;

procedure TForm1.lineCbDropDown(Sender: TObject);
begin
  workOrderCb.items.Clear;
  boardTypeCb.items.Clear;
end;

procedure TForm1.workOrderCbDropDown(Sender: TObject);
begin
  boardTypeCb.items.Clear;
end;

procedure TForm1.Button1Click(Sender: TObject);
begin
  if (boardTypeCb.Text = '')then
  begin
     MessageBox(Handle, '�㻹û��ѡ�񹤵�', '��ʾ', MB_OK + MB_ICONERROR);
     Exit;
  end;
  if MessageBox(Handle, '��ȷ��Ҫ���øù�����״̬������ʼ�����������������״̬', '����', MB_OKCANCEL + MB_ICONWARNING) = IDOK then
  begin
     requestHttp('reset',false);
  end;
end;

function TForm1.getBoardTypeNo(Name:string) : string;
begin
    //������������
    if (boardTypeCb.Text = 'Ĭ��') then
    begin
      getBoardTypeNo := '0';
    end
    else if (boardTypeCb.Text = 'AB��') then
    begin
      getBoardTypeNo := '1';
    end
    else if (boardTypeCb.Text = 'A��') then
    begin
      getBoardTypeNo := '2';
    end
    else if (boardTypeCb.Text = 'B��') then
    begin
      getBoardTypeNo := '3';
    end;
end;

procedure TForm1.requestHttp(action: string; isClose:Boolean);
var
  IdHttp : TIdHTTP;
  Url : string;//�����ַ
  ResponseStream : TStringStream; //������Ϣ
  ResponseStr : string;
  RequestList : TStringList;     //������Ϣ
begin
  //����IDHTTP�ؼ�
  IdHttp := TIdHTTP.Create(nil);
  //TStringStream�������ڱ�����Ӧ��Ϣ
  ResponseStream := TStringStream.Create('');
  RequestList := TStringList.Create;
  try
    Url := 'http://10.10.11.109:8080/eps_server/program/' + action;
    try
      //���б�ķ�ʽ�ύ����
      RequestList.Add('line='+lineCb.Text);
      if(isClose = false)then
      begin
        RequestList.Add(AnsiToUTF8('workOrder='+workOrderCb.Text));
        RequestList.Add(AnsiToUTF8('boardType='+getBoardTypeNo(boardTypeCb.Text)));
      end;
      IdHttp.Post(Url,RequestList,ResponseStream);
    except
      on e : Exception do
      begin
        ShowMessage(e.Message);
      end;
    end;
    //��ȡ��ҳ���ص���Ϣ
    ResponseStr := ResponseStream.DataString;
    if(ResponseStr <> '{"result":"succeed"}')then
    begin
      ShowMessage(ResponseStr);
    end;
  finally
    IdHttp.Free;
    RequestList.Free;
    ResponseStream.Free;
  end;
end;

procedure TForm1.FormDestroy(Sender: TObject);
begin
  requestHttp('switch', true);
end;

end.

