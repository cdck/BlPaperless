package com.pa.paperless.utils;

import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.pa.paperless.data.bean.ImportVoteBean;
import com.pa.paperless.data.bean.SignInBean;
import com.pa.paperless.data.bean.VoteBean;
import com.pa.paperless.data.constant.EventMessage;
import com.pa.paperless.data.constant.EventType;
import com.pa.paperless.data.constant.Macro;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static com.pa.paperless.utils.FileUtil.CreateDir;

/**
 * Created by Administrator on 2018/2/28.
 */

public class Export {
    /**
     * 以xls表格的形式导出签到信息
     *
     * @param fileName  创建的表格文件名
     * @param sheetName 工作表名称
     * @param titles    每一列的标题
     * @param datas     签到数据
     */
    public static boolean ToSigninExcel(String fileName, String sheetName, String[] titles, List<SignInBean> datas) {
        boolean succeed = true;
        try {
            //1.创建Excel文件
            CreateDir(Macro.VOTE_RESULT);
            File file = new File(Macro.VOTE_RESULT + fileName + ".xls");
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建sheet  int型参数 ：代表sheet号，0是第一页，1是第二页以此类推
            WritableSheet sheet = workbook.createSheet(sheetName, 0);
            //4.单元格
            Label label = null;
            //5.给第一行设置列名
            for (int i = 0; i < titles.length; i++) {
                // 三个参数分别表示： 列 行 文本内容
                label = new Label(i, 0, titles[i]);
                //6.添加单元格
                sheet.addCell(label);
            }
            //7.导入数据 i 从1开始
            for (int i = 1; i <= datas.size(); i++) {
                //获取正确的数据 i-1  不然会索引越界
                SignInBean bean = datas.get(i - 1);
                for (int j = 0; j < titles.length; j++) {
                    // j 表示的是列数
                    String str = "";
                    switch (j) {
                        case 0:
                            str = bean.getSignin_num();
                            break;
                        case 1:
                            str = bean.getSignin_name();
                            break;
                        case 2:
                            str = bean.getSignin_date();
                            break;
                        case 3:
                            if (bean.getSignin_date() != null && !(bean.getSignin_date().isEmpty())) {
                                str = "已签到";
                            } else {
                                str = " ";
                            }
                            break;
                    }
                    //8.添加数据
                    label = new Label(j, i, str);
                    sheet.addCell(label);
                }
            }
            //9.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //10.最后一步，关闭工作簿
            workbook.close();
            EventBus.getDefault().post(new EventMessage(EventType.export_finish, file, "export"));
        } catch (Exception e) {
            succeed = false;
            e.printStackTrace();
        }
        return succeed;
    }

    /**
     * 导出选举
     *  @param fileName
     * @param titles
     * @param data
     */
    public static void VoteEntry(String fileName, String[] titles, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data, int type) {
        //创建文件导出目录
        CreateDir(Macro.VOTE_RESULT);
        File file = new File(Macro.VOTE_RESULT + fileName + ".xls");
        try {
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建sheet  int型参数 ：代表sheet号，0是第一页，1是第二页以此类推
            WritableSheet sheet = workbook.createSheet("Sheet1", 0);
            //4.单元格
            Label label = null;
            //5.给第一行设置列名
            for (int i = 0; i < titles.length; i++) {
                // 三个参数分别表示： 列 行 文本内容
                label = new Label(i, 0, titles[i]);
                //6.添加单元格
                sheet.addCell(label);
            }
            if (type == 0) {//投票
                //7.导入数据  i从1开始，因为第0行已经是标题导航
                for (int i = 1; i < data.size() + 1; i++) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = data.get(i - 1);
                    String str = "";
                    //8.j表示列的索引
                    for (int j = 0; j < titles.length; j++) {
                        switch (j) {
                            case 0:
                                str = voteInfo.getContent().toStringUtf8();
                                break;
                            case 1:
                                str = voteInfo.getMode() + "";
                                break;
                        }
                        //8.添加数据
                        label = new Label(j, i, str);
                        sheet.addCell(label);
                    }
                }
            } else {//选举
                for (int i = 1; i < data.size() + 1; i++) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = data.get(i - 1);
                    String labelStr = "";
                    for (int j = 0; j < titles.length; j++) {
                        switch (j) {
                            case 0://选举内容
                                labelStr = voteInfo.getContent().toStringUtf8();
                                break;
                            case 1://是否记名
                                labelStr = voteInfo.getMode() + "";
                                break;
                            case 2://选项总数量
                                labelStr = voteInfo.getItemList().size() + "";
                                break;
                            case 3://答案数量
                                labelStr = getAnswerCount(voteInfo.getType(), voteInfo.getItemList().size()) + "";
                                break;
                            case 4://选项一
                                labelStr = voteInfo.getItemList().size() > 0 ? voteInfo.getItemList().get(0).getText().toStringUtf8() : "";
                                break;
                            case 5:
                                labelStr = voteInfo.getItemList().size() > 1 ? voteInfo.getItemList().get(1).getText().toStringUtf8() : "";
                                break;
                            case 6:
                                labelStr = voteInfo.getItemList().size() > 2 ? voteInfo.getItemList().get(2).getText().toStringUtf8() : "";
                                break;
                            case 7:
                                labelStr = voteInfo.getItemList().size() > 3 ? voteInfo.getItemList().get(3).getText().toStringUtf8() : "";
                                break;
                            case 8:
                                labelStr = voteInfo.getItemList().size() > 4 ? voteInfo.getItemList().get(4).getText().toStringUtf8() : "";
                                break;
                        }
                        //8.添加数据
                        label = new Label(j, i, labelStr);
                        sheet.addCell(label);
                    }
                }
            }
            //9.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //10.最后一步，关闭工作簿
            workbook.close();
            EventBus.getDefault().post(new EventMessage(EventType.EXPORT_VOTEENTRY_FINISH, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到可以选择的答案数量
     *
     * @param type 5选3,5选2 ...
     * @param all  多选的时候 返回拥有答案的数量
     * @return
     */
    private static int getAnswerCount(int type, int all) {
        if (type == 1) return 1;
        else if (type == 2) return 4;
        else if (type == 3) return 3;
        else if (type == 4) return 2;
        else if (type == 5) return 2;
        else return all;
    }


    /**
     * 以xls表格的形式导出投票结果信息
     *
     * @param content
     * @param fileName
     * @param SheetName
     * @param titles
     * @param datas
     * @return 返回true则表示导出成功
     */
    public static boolean ToVoteResultExcel(String content, String fileName, String SheetName, String[] titles, List<VoteBean> datas) {
        boolean succeed = true;
        CreateDir(Macro.VOTE_RESULT);
        // 1.创建Excel文件
        File file = new File(content + fileName + ".xls");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            // 2.创建工作薄
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            // 3.创建Sheet
            WritableSheet sheet = workbook.createSheet(SheetName, 0);
            // 4.创建单元格
            Label label = null;
            // 第一行展示投票内容
            Label titLabel = new Label(0, 0, content);
            sheet.addCell(titLabel);
            for (int i = 0; i < titles.length; i++) {
                // 5.定位 列 行 内容
                label = new Label(i, 1, titles[i]);
                // 6.添加单元格
                sheet.addCell(label);
            }
            // 7.导入数据 第一行是标题  所以从第二行开始
            for (int i = 2; i < datas.size() + 2; i++) {
                VoteBean voteBean = datas.get(i - 2);
                for (int j = 0; j < titles.length; j++) {
                    String str = "";
                    switch (j) {
                        case 0:
                            str = i - 1 + "";
                            break;
                        case 1:
                            str = voteBean.getName();
                            break;
                        case 2:
                            str = voteBean.getChoose();
                            break;
                    }
                    //8.添加数据  列 行 数据
                    label = new Label(j, i, str);
                    sheet.addCell(label);
                }
            }
            //9.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //10.最后一步，关闭工作簿
            workbook.close();
            EventBus.getDefault().post(new EventMessage(EventType.export_finish, file, "export"));
        } catch (IOException e) {
            succeed = false;
            e.printStackTrace();
        } catch (WriteException e) {
            succeed = false;
            e.printStackTrace();
        }
        return succeed;
    }

    /**
     * 以Excel表格的形式导出投票结果信息
     *
     * @param meetName  当前参加的会议名称
     * @param fileName  文件名
     * @param SheetName 工作表名称
     * @param titles    每一列的标题
     * @param datas     投票数据
     * @return
     */
    public static boolean ToVoteExcel(String meetName, String fileName, String SheetName, String[] titles, List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> datas) {
        boolean succeed = true;
        CreateDir(Macro.VOTE_RESULT);
        //1.创建Excel文件
        File file = new File(Macro.VOTE_RESULT + meetName + fileName + ".xls");
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            //2.创建工作簿
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            //3.创建Sheet
            WritableSheet sheet = workbook.createSheet(SheetName, 0);
            //4.创建单元格
            Label label = null;
            //首先在第一行的位置添加 会议名作为主题
            Label titLable = new Label(0, 0, meetName);
            sheet.addCell(titLable);
            for (int i = 0; i < titles.length; i++) {
                //5.定为 列 行 文本内容
                label = new Label(i, 1, titles[i]);
                //6.添加单元格
                sheet.addCell(label);
            }
            //7.导入数据 因为第一行是自定义的标题文本  所以 i从1（第二行）开始
            //表格格式   第一行 投票结果信息
            //          第二行 内容 id ...
            //          之后才是数据 ...
            for (int i = 2; i < datas.size() + 2; i++) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = datas.get(i - 2);
                List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = voteInfo.getItemList();
                for (int j = 0; j < titles.length; j++) {
                    //j表示的是列数
                    String str = "";
                    switch (j) {
                        case 0:
                            str = voteInfo.getVoteid() + "";
                            break;
                        case 1:
                            str = voteInfo.getContent().toStringUtf8();
                            break;
                        case 2:
                            int type = voteInfo.getType();
                            if (type == 0) {
                                str = "多选";
                            } else if (type == 1) {
                                str = "单选";
                            } else if (type == 2) {
                                str = "5选4";
                            } else if (type == 3) {
                                str = "5选3";
                            } else if (type == 4) {
                                str = "5选2";
                            } else if (type == 5) {
                                str = "3选2";
                            }
                            break;
                        case 3:
                            if (voteInfo.getMode() == 0) {
                                str = "匿名";
                            } else {
                                str = "记名";
                            }
                            break;
                        case 4:
                            int votestate = voteInfo.getVotestate();
                            if (votestate == 0) {
                                str = "未发起";
                            }
                            if (votestate == 1) {
                                str = "正在投票";
                            }
                            if (votestate == 2) {
                                str = "已经结束";
                            }
                            break;
                        case 5: //选项一
                            if (optionInfo.size() >= 1) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(0);
                                str = voteOptionsInfo.getText().toStringUtf8();
                            }
                            break;
                        case 6: //投票数
                            if (optionInfo.size() >= 1) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(0);
                                int selcnt = voteOptionsInfo.getSelcnt();
                                str = selcnt + "";
                            }
                            break;
                        case 7://选项二
                            if (optionInfo.size() >= 2) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(1);
                                str = voteOptionsInfo.getText().toStringUtf8();
                            }
                            break;
                        case 8://投票数
                            if (optionInfo.size() >= 2) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(1);
                                int selcnt = voteOptionsInfo.getSelcnt();
                                str = selcnt + "";
                            }
                            break;
                        case 9://选项三
                            if (optionInfo.size() >= 3) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(2);
                                str = voteOptionsInfo.getText().toStringUtf8();
                            }
                            break;
                        case 10://投票数
                            if (optionInfo.size() >= 3) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(2);
                                int selcnt = voteOptionsInfo.getSelcnt();
                                str = selcnt + "";
                            }
                            break;
                        case 11://选项四
                            if (optionInfo.size() >= 4) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(3);
                                str = voteOptionsInfo.getText().toStringUtf8();
                            }
                            break;
                        case 12://投票数
                            if (optionInfo.size() >= 4) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(3);
                                int selcnt = voteOptionsInfo.getSelcnt();
                                str = selcnt + "";
                            }
                            break;
                        case 13://选项五
                            if (optionInfo.size() >= 5) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(4);
                                str = voteOptionsInfo.getText().toStringUtf8();
                            }
                            break;
                        case 14://投票数
                            if (optionInfo.size() >= 5) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(4);
                                int selcnt = voteOptionsInfo.getSelcnt();
                                str = selcnt + "";
                            }
                            break;
                    }
                    //8.添加数据  列 行 数据
                    label = new Label(j, i, str);
                    sheet.addCell(label);
                }
            }
            //9.写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
            workbook.write();
            //10.最后一步，关闭工作簿
            workbook.close();
            EventBus.getDefault().post(new EventMessage(EventType.export_finish, file, "export"));
        } catch (IOException e) {
            succeed = false;
            e.printStackTrace();
        } catch (WriteException e) {
            succeed = false;
            e.printStackTrace();
        }
        return succeed;
    }

    /**
     * 外部导入投票
     *
     * @param path 文件路径
     */
    public static List<ImportVoteBean> ReadVoteExcel(String path) {
        LogUtil.e("Export", "Export.ReadVoteExcel :  导入文件 --> " + path);
        List<ImportVoteBean> line = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return line;
        //1:创建workbook
        // new File("test.xls")
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        //2:获取第一个工作表sheet
        Sheet sheet = workbook.getSheet(0);
        //3:获取数据
        int rows = sheet.getRows();
        int columns = sheet.getColumns();
        LogUtil.e("Export", "Export.ReadVoteExcel :   --> 共 " + rows + " 行 " + columns + " 列");
        for (int i = 1; i < rows; i++) {
            ImportVoteBean importVoteBean = new ImportVoteBean();
            for (int j = 0; j < columns; j++) {
                //4.获取定位的单元格
                Cell cell = sheet.getCell(j, i);
                //5.获取该单元格的文本内容
                String contents = cell.getContents();
                LogUtil.e("Export", "Export.ReadVoteExcel :  当前单元格内容 --> " + contents);
                if (j == 0) {
                    importVoteBean.setContent(contents);
                } else if (j == 1) {
                    //每一行只需要两个数据,所以跳出当前列循环，去下一行获取前两列数据
                    importVoteBean.setMode(contents.equals("1") ? 1 : 0);
                    line.add(importVoteBean);
                    break;
                }
            }
        }
        LogUtil.e("MyLog", "Export.ReadExcel 146行:  一共多少个数据 --->>> " + line.size());
        //最后一步：关闭资源
        workbook.close();
        return line;
    }

    /**
     * 导入选举
     *
     * @param path
     * @return
     */
    public static List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> importSurvey(String path) {
        LogUtil.e("Export", "Export.ReadVoteExcel :  导入文件 --> " + path);
        List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> line = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return line;
        //1:创建workbook
        // new File("test.xls")
        Workbook workbook = null;
        try {
            workbook = Workbook.getWorkbook(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        //2:获取第一个工作表sheet
        Sheet sheet = workbook.getSheet(0);
        //3:获取数据
        int rows = sheet.getRows();
        int columns = sheet.getColumns();
        LogUtil.e("Export", "Export.ReadVoteExcel :   --> 共 " + rows + " 行 " + columns + " 列");
        for (int i = 1; i < rows; i++) {
            /** **** **  每到新的一行，就新创建一个投票  ** **** **/
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
            int optionCount = 0;
            int answerCount = 0;
            List<ByteString> chooses = null;
            for (int j = 0; j < columns; j++) {
                //4.获取定位的单元格
                Cell cell = sheet.getCell(j, i);
                //5.获取该单元格的文本内容
                String contents = cell.getContents();
                LogUtil.e("Export", "Export.ReadVoteExcel :  当前单元格内容 --> " + contents);
                switch (j) {
                    case 0://设置选举内容
                        chooses = new ArrayList<>();
                        builder.setContent(MyUtils.s2b(contents));
                        break;
                    case 1://是否记名
                        builder.setMode(contents.equals("1") ? 1 : 0);
                        break;
                    case 2://选项总数
                        optionCount = Integer.parseInt(contents);
                        LogUtil.i("Export", "Export.importSurvey :  选项总数 --> " + optionCount);
                        break;
                    case 3://答案总数
                        answerCount = Integer.parseInt(contents);
                        LogUtil.d("Export", "Export.importSurvey :  答案总数 --> " + answerCount);
                        if (answerCount == 1) {
                            builder.setType(1);//单选
                            break;
                        }
                        if (optionCount == 3) {
                            if (answerCount == 2) {
                                builder.setType(5);//3选2
                                break;
                            } else if (answerCount > 2) {
                                builder.setType(0);//多选
                                break;
                            }
                        }
                        if (optionCount == 4) {
                            if (answerCount > 1) {
                                builder.setType(0);//多选
                                break;
                            }
                        }
                        if (optionCount == 5) {
                            if (answerCount == 2) {
                                builder.setType(4);//5选2
                                break;
                            } else if (answerCount == 3) {
                                builder.setType(3);//5选3
                                break;
                            } else if (answerCount == 4) {
                                builder.setType(2);//5选4
                                break;
                            } else {
                                builder.setType(0);//多选
                                break;
                            }
                        }
                        break;
                    case 4://选项一
                        chooses.add(MyUtils.s2b(contents));
                        break;
                    case 5://选项二
                        chooses.add(MyUtils.s2b(contents));
                        break;
                    case 6://选项三
                        chooses.add(MyUtils.s2b(contents));
                        break;
                    case 7://选项四
                        chooses.add(MyUtils.s2b(contents));
                        break;
                    case 8://选项五
                        chooses.add(MyUtils.s2b(contents));
                        break;
                }
            }
            builder.setMaintype(1);
            builder.setSelectcount(chooses.size());
            builder.addAllText(chooses);
            InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
            line.add(build);
        }
        LogUtil.e("MyLog", "Export.ReadExcel 146行:  一共多少个数据 --->>> " + line.size());
        //最后一步：关闭资源
        workbook.close();
        return line;
    }

    /**
     * 将String以txt文本格式导出保存
     *
     * @param content
     * @param fileName
     */
    public static boolean ToNoteText(String content, String fileName, String filepath) {
        File dir = new File(filepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(filepath, fileName + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean b = true;
        try {
            FileWriter fw = new FileWriter(filepath + fileName + ".txt");
            fw.flush();
            fw.write(content);
            fw.close();
            LogUtil.i("ToNoteText->", " 导出笔记成功 -->");
            EventBus.getDefault().post(new EventMessage(EventType.export_finish, file, "cache"));
        } catch (IOException e) {
            b = false;
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 读取TXT文本文件
     *
     * @param file .txt文本文件
     * @return 返回字符串形式
     */
    public static String readText(File file) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtil.e("MyLog", "Export.readText 412行:  读取txt文件内容 --->>> " + sb.toString());
        return sb.toString();
    }

}
