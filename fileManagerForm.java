package fileManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class fileManagerForm extends JFrame
{
	private JPanel panel;
	private JLabel cataPathJLabel;
	private JLabel cataJLabel;
	private JLabel fileJLabel;
	private JButton exitButton;
	private JButton createCataButton;
	private JButton deleteCataButton;
	private JButton enterCataButton;
	private JButton parentCataButton;
	private JButton createFileButton;
	private JButton deleteFileButton;
	private JButton saveFileButton;
	private JButton resetFileButton;
	private JComboBox cataBox;
	private JComboBox fileBox;
	private JTextArea cataInfoArea;
	private JTextArea fileInfoArea;
	private JTextField cataPath;
	private fileManager file;

	public fileManagerForm()
	{
		fileInit();
		fileManagerFormInit();

	}

	public void fileInit() // 文件管理系统初始化
	{
		file = new fileManager();

	}

	private void boxInfoInit() // 下拉项控件初始化
	{
		// 清除原有的item
		cataBox.removeAllItems();
		fileBox.removeAllItems();
		// 获取当前目录下的第一个子目录位置
		int cataPosition = file.getSonCata(file.getNowCP());
		if (cataPosition == file.FINALCATA)
		{
			return;
		}

		while (file.getNextCP(cataPosition) != file.FINALCATA)
		{
			int a = file.getType(cataPosition);
			if (file.getType(cataPosition) == file.CATATYPE)
			{
				String aString = file.getCataName(cataPosition);
				cataBox.addItem(file.getCataName(cataPosition));
			} else
			{
				String aString = file.getCataName(cataPosition);
				fileBox.addItem(aString);
			}
			cataPosition = file.getNextCP(cataPosition);
		}
		if (file.getType(cataPosition) == file.CATATYPE)
		{
			String aString = file.getCataName(cataPosition);
			cataBox.addItem(file.getCataName(cataPosition));
		} else
		{
			String aString = file.getCataName(cataPosition);
			fileBox.addItem(file.getCataName(cataPosition));
		}
	}

	public void fileManagerFormInit() // GUI界面初始化
	{

		setSize(635, 500);
		setTitle("File Management");
		setResizable(false);
		setLocation(100, 100);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();
		panel.setLayout(null);
		cataPathJLabel = new JLabel("当前目录");
		cataJLabel = new JLabel("文件夹");
		fileJLabel = new JLabel("文件");
		exitButton = new JButton("退出系统");
		createCataButton = new JButton("新建");
		deleteCataButton = new JButton("删除");
		enterCataButton = new JButton("进入");
		parentCataButton = new JButton("上级");
		createFileButton = new JButton("新建");
		deleteFileButton = new JButton("删除");
		saveFileButton = new JButton("保存");
		resetFileButton = new JButton("重写");
		cataBox = new JComboBox();
		fileBox = new JComboBox();
		cataInfoArea = new JTextArea(8, 40);
		fileInfoArea = new JTextArea(8, 40);
		cataPath = new JTextField();

		cataPathJLabel.setSize(100, 20);
		cataPathJLabel.setLocation(10, 10);

		exitButton.setSize(100, 25);
		exitButton.setLocation(230, 30);

		cataPath.setSize(200, 20);
		cataPath.setLocation(10, 30);
		cataPath.setEditable(false);

		cataJLabel.setSize(100, 20);
		cataJLabel.setLocation(10, 110);

		cataBox.setSize(200, 20);
		cataBox.setLocation(10, 130);

		fileJLabel.setSize(100, 20);
		fileJLabel.setLocation(350, 110);

		fileBox.setSize(200, 20);
		fileBox.setLocation(350, 130);

		createCataButton.setSize(60, 30);
		createCataButton.setLocation(10, 70);

		deleteCataButton.setSize(60, 30);
		deleteCataButton.setLocation(80, 70);

		enterCataButton.setSize(60, 30);
		enterCataButton.setLocation(150, 70);

		parentCataButton.setSize(60, 30);
		parentCataButton.setLocation(220, 70);

		createFileButton.setSize(60, 30);
		createFileButton.setLocation(350, 70);

		deleteFileButton.setSize(60, 30);
		deleteFileButton.setLocation(420, 70);

		saveFileButton.setSize(60, 30);
		saveFileButton.setLocation(490, 70);

		resetFileButton.setSize(60, 30);
		resetFileButton.setLocation(560, 70);

		cataInfoArea.setLineWrap(true);
		cataInfoArea.setEditable(false);
		cataInfoArea.setSize(200, 250);
		cataInfoArea.setLocation(10, 180);

		fileInfoArea.setLineWrap(true);
		fileInfoArea.setSize(200, 250);
		fileInfoArea.setLocation(350, 180);

		panel.add(cataBox);
		panel.add(cataInfoArea);
		panel.add(cataJLabel);
		panel.add(cataPath);
		panel.add(cataPathJLabel);
		panel.add(createCataButton);
		panel.add(createFileButton);
		panel.add(deleteCataButton);
		panel.add(deleteFileButton);
		panel.add(enterCataButton);
		panel.add(exitButton);
		panel.add(fileBox);
		panel.add(fileInfoArea);
		panel.add(fileJLabel);
		panel.add(parentCataButton);
		panel.add(resetFileButton);
		panel.add(saveFileButton);
		addListener();
		file.pathUpdate();
		cataPath.setText(file.path);
		boxInfoInit();
		add(panel);
		setVisible(true);
	}

	private void addListener() // 控件添加监听事件
	{
		createCataButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String fileName = JOptionPane.showInputDialog("请输入文件夹名：");
				if (fileName.length() > 0 && fileName.length() < 16)
				{
					FilePosition position = file.findCataPosition(fileName,
							file.CATATYPE);
					if (position.getNowPosition() == file.NULL
							&& position.getPrePosition() == file.NULL)
					{
						file.createCatalogue(fileName);
						cataBox.addItem(fileName);
						return;
					} else
					{
						JOptionPane.showMessageDialog(panel, "已经相同名称的文件");
					}

				}
				if (fileName.length() == 0)
				{
					JOptionPane.showMessageDialog(panel, "文件夹名不能为空");
					return;
				}
				if (fileName.length() > 16)
				{
					JOptionPane.showMessageDialog(panel, "文件夹名过长");
					return;
				}

			}
		});
		createFileButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String fileName = JOptionPane.showInputDialog("请输入文件名：");
				if (fileName.length() > 0 && fileName.length() < 16)
				{
					FilePosition position = file.findCataPosition(fileName,
							file.FILETYPE);
					if (position.getNowPosition() == file.NULL
							&& position.getPrePosition() == file.NULL)
					{
						file.createFile(fileName);
						fileBox.addItem(fileName);
						return;
					} else
					{
						JOptionPane.showMessageDialog(panel, "已经相同名称的文件");
					}

				}
				if (fileName.length() == 0)
				{
					JOptionPane.showMessageDialog(panel, "文件夹名不能为空");
					return;
				}
				if (fileName.length() > 16)
				{
					JOptionPane.showMessageDialog(panel, "文件夹名过长");
					return;
				}
			}
		});

		cataBox.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{

				cataInfoArea.setText("");
				cataInfoArea.append("------------------");
				cataInfoArea.append("\n");
				FilePosition cataPosition = file.findCataPosition(
						(String) cataBox.getSelectedItem(), file.CATATYPE);
				if (cataPosition.getNowPosition() == file.NULL
						&& cataPosition.getPrePosition() == file.NULL)
				{
					cataInfoArea.append("\n");
					cataInfoArea.append("------------------");
				} else
				{
					cataInfoArea.append("文件夹的位置：");

					cataInfoArea.append(Integer.toString(cataPosition
							.getNowPosition()));
					cataInfoArea.append("\n");
					cataInfoArea.append("文件下子项目数量：");
					cataInfoArea.append(Integer.toString(file
							.getSonCataSize(cataPosition.getNowPosition())));
					cataInfoArea.append("\n");
					cataInfoArea.append("------------------");
				}

			}
		});

		fileBox.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				FilePosition filePosition = file.findCataPosition(
						(String) fileBox.getSelectedItem(), file.FILETYPE);
				if (filePosition.getNowPosition() == file.NULL
						&& filePosition.getPrePosition() == file.NULL) // 如果找不到该文件
				{
					return;
				} else
				{
					fileInfoArea.setText(file.readFile((String) fileBox
							.getSelectedItem()));
				}

			}
		});
		exitButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				file.writeData2Local();
				System.exit(0);

			}
		});
		resetFileButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				fileInfoArea.setText("");
			}
		});
		saveFileButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String data = fileInfoArea.getText();
				String fileName = (String) fileBox.getSelectedItem();
				if (fileName.length() == 0)
				{
					JOptionPane.showMessageDialog(panel, "请选择文件");
				} else
				{
					file.writeFile(fileName, data);

				}
			}
		});
		deleteCataButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String cataName = (String) cataBox.getSelectedItem();
				if (cataName.length() == 0)
				{
					JOptionPane.showMessageDialog(panel, "请选择文件夹！");
				} else
				{
					file.deleteCatalogue(cataName);
					cataBox.removeItem(cataName);
				}
			}
		});
		deleteFileButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				String cataName = (String) fileBox.getSelectedItem();
				if (cataName.length() == 0)
				{
					JOptionPane.showMessageDialog(panel, "请选择文件！");
				} else
				{
					file.deleteFile(cataName);
					fileBox.removeItem(cataName);
				}
			}
		});

		enterCataButton.addActionListener(new ActionListener()
		{
			// 进入文件夹事件
			public void actionPerformed(ActionEvent arg0)
			{
				String cataName = (String) cataBox.getSelectedItem();
				if (cataName == null) // 若文件夹名称为空
				{
					JOptionPane.showMessageDialog(panel, "请选择文件！");
					return;
				} else
				{

					file.enterCata(cataName);// 进入文件夹
					file.pathUpdate();// 更新路径
					cataPath.setText(file.path);
					boxInfoInit();// 更新下拉栏
					fileInfoArea.setText("");
				}
			}
		});

		parentCataButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{
				file.parentCata();
				file.pathUpdate();
				cataPath.setText(file.path);
				boxInfoInit();

			}
		});

	}

	public static void main(String[] args)
	{
		fileManagerForm run = new fileManagerForm();

		// TODO 自动生成的方法存根

	}

}
