package fileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class fileManager
{
	private char[][] disk; // 磁盘存储，为128*256的char数组
	private char[] FAT; // FAT表格，为1*128的char数组
	private char[][] catalogue; // 目录表，为256*32的char数组，目录表有文件和文件夹两种文件类型
	private char[] nowCata; // 目前的目录项
	public final char FILETYPE = 'f'; // 表示类型为文件
	public final char CATATYPE = 'c'; // 表示类型为文件夹
	public final char DISKTYPE = 'd';// 表示该类型为磁盘
	public final char NULL = (char) 0; // 表示该字符为“空”
	public final int TYPEPOSITION = 16; // 该数字表述目录项中存储类型信息的数组位置
	public final int SIZEPOSITION = 17; // 表示目录项中存储大小信息的数组位置
	// 若为文件，该位置存储的大小信息表示文件数据所占有的盘块数，若为文件夹，表示该文件下有多少个目录项。
	public final int FIRSTBLOCK = 18; // 若为文件类型，该数字表示文件数据首盘块在disk的位置，
	// 若为文件夹类型，表示文件夹下的第一个目录项在catalogue的位置
	public final int PARENTCATA = 19; // 表示该目录项的父目录在catalogue数组的位置
	public final int NEXTCATA = 20; // 表示同一父目录下的邻接目录项所在位置
	private int NOWCATAPOSITION; // 表示目前所在目录项在catalogue数组的位置
	public final int FINALBLOCK = 127; // 尾目录项标识
	public final int FINALCATA = 127; // 尾盘块标识
	public final int DATASIZE = 256; // 磁盘块大小
	public final int CATASIZE = 32; // 目录表数据结构大小
	public final int CATANAMESIZE = 16;// 最大文件名长度
	public String path; // 文件路径
	private final String dataPath = "dataFile.hjp"; // 数据保存路径

	public fileManager() // 构造函数
	{
		disk = new char[FINALBLOCK][DATASIZE];
		FAT = new char[FINALBLOCK];
		catalogue = new char[FINALCATA][CATASIZE];
		if (!readLocalData())
		{
			arrayInit();
		}

	}

	private void deleteCatalogue(int position) // 根据提供的目录项位置删除文件夹
	{
		// 首先删除该目录项下的所有子目录项，运用的递归思想

		if (catalogue[position][SIZEPOSITION] == NULL)
		{
			// 如果该文件夹没有文件，则直接删除自身
			deleteArray(position, CATASIZE);
			return;
		}
		int temp = (int) catalogue[position][FIRSTBLOCK];
		while (catalogue[temp][NEXTCATA] != FINALCATA) // 当子目录项不是最后一个目录项时
		{
			int nextPositon = catalogue[temp][NEXTCATA];// 存储下一个要删除的目录项的位置信息
			if (catalogue[temp][TYPEPOSITION] == CATATYPE) // 若要删除的目录项类型为文件夹
			{
				deleteCatalogue(temp);
			} else
			// 若要删除的目录项类型为文件
			{
				deleteFile(temp);
			}

			temp = nextPositon;

		}
		// 删除最后一个子目录项
		if (catalogue[temp][TYPEPOSITION] == CATATYPE)
		{
			deleteCatalogue(temp);
		} else
		{
			deleteFile(temp);
		}
		// 删除自身
		deleteArray(position, CATASIZE);

	}

	private int malloc(int type)// 根据type类型分配相应的空数组
	{

		if (type == CATATYPE)
		{
			int randNum = (int) (Math.random() * FINALCATA);
			while (catalogue[randNum][0] != NULL)// 判断随机产生的位置是否为空目录项
			{
				randNum = (int) (Math.random() * FINALCATA);
			}
			return randNum;
		} else
		{
			int randNum = (int) (Math.random() * FINALBLOCK);
			while (FAT[randNum] != NULL)// 判断随机产生的位置是否为空目录项
			{
				randNum = (int) (Math.random() * FINALBLOCK);
			}
			return randNum;
		}
	}

	private void deleteDiskData(int position)// 将指定位置的盘块清空
	{
		for (int i = 0; i < DATASIZE; i++)
		{
			disk[position][i] = NULL;
		}
	}

	private void deleteFile(int Cataposition)// 通过指定文件所在的目录数组位置来删除文件在disk的数据
	{
		// 获取文件在disk的首盘块地址
		int diskPosition = catalogue[Cataposition][FIRSTBLOCK];
		int temp;
		if (diskPosition == FINALBLOCK)
		{
			return;
		}
		while (FAT[diskPosition] != FINALBLOCK) // 通过链表逐一删除文件在盘块的数据
		{
			deleteDiskData(diskPosition);
			temp = FAT[diskPosition];
			FAT[diskPosition] = NULL; // 将FAT记录的盘块信息置空
			diskPosition = temp;
		}
		deleteDiskData(diskPosition);
		FAT[diskPosition] = NULL;
	}

	private void arrayInit() // 如果没有本地数据，则调用该函数初始化数据
	{
		for (int i = 0; i < FINALBLOCK; i++)
		{
			FAT[i] = (char) 0;
			for (int j = 0; j < DATASIZE; j++)
			{
				disk[i][j] = (char) 0;
			}
		}
		for (int i = 0; i < FINALCATA; i++)
		{
			for (int j = 0; j < CATASIZE; j++)
			{
				catalogue[i][j] = (char) 0;
			}
		}
		// 根目录初始化
		catalogue[0][0] = 'r';
		catalogue[0][1] = 'o';
		catalogue[0][2] = 'o';
		catalogue[0][3] = 't';
		catalogue[0][TYPEPOSITION] = 'f';
		catalogue[0][FIRSTBLOCK] = (char) FINALCATA;
		nowCata = catalogue[0];

		NOWCATAPOSITION = 0;
	}

	private void deleteArray(int position, int length)// 清空指定的数组数据
	{
		if (length == 32)
		{
			for (int i = 0; i < length; i++)
			{
				catalogue[position][i] = NULL;
			}
		}
		if (length == 256)
		{
			for (int i = 0; i < length; i++)
			{
				disk[position][i] = NULL;
			}
		}
	}

	private boolean isSameName(int position, String name) // 判断指定位置的文件名是否和name相同
	{
		int i;
		for (i = 0; i < name.length(); i++)
		{
			if (catalogue[position][i] != name.charAt(i))
			{
				return false;
			}
		}
		if (catalogue[position][i] == NULL)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public FilePosition findCataPosition(String cataName, int type) // 寻找当前目录项的文件位置
	{
		if (cataName == null
				|| catalogue[NOWCATAPOSITION][FIRSTBLOCK] == FINALCATA
				|| cataName.length() == 0)
		{
			FilePosition position = new FilePosition(NULL, NULL);
			return position;
		}
		int preCataPositon = NOWCATAPOSITION;// 记录所搜索的目录项的前一目录项的位置
		int nowPosition = catalogue[preCataPositon][FIRSTBLOCK]; // 记录所所搜索的目录项的位置
		// 当文件名和文件类型相等时，判断找到该文件
		while (!(isSameName(nowPosition, cataName) && (int) catalogue[nowPosition][TYPEPOSITION] == type)
				&& catalogue[nowPosition][NEXTCATA] != FINALCATA) // 循环直到找到要删除的目录项
		{
			preCataPositon = nowPosition;
			nowPosition = catalogue[nowPosition][NEXTCATA];
		}
		// 找到文件
		if ((isSameName(nowPosition, cataName) && (int) catalogue[nowPosition][TYPEPOSITION] == type))
		{
			FilePosition position = new FilePosition(preCataPositon,
					nowPosition);
			return position;

		} else
		// 找不到文件
		{
			FilePosition position = new FilePosition(NULL, NULL);
			return position;
		}

	}

	public boolean fileExist(String cataName, int type) // 判断文件是否存在
	{
		FilePosition filePosition = findCataPosition(cataName, type);
		if (filePosition.getNowPosition() == NULL
				&& filePosition.getPrePosition() == NULL)
		{
			return false;
		} else
		{
			return true;
		}
	}

	public void createCatalogue(String cataString) // 新建文件夹
	{
		// 创建的目录项在数组的位置是随机产生的
		int randNum = malloc(CATATYPE);
		// 在catalogue空目录项存储相应的信息
		for (int i = 0; i < cataString.length(); i++)
		{
			catalogue[randNum][i] = cataString.charAt(i);
		}
		catalogue[randNum][FIRSTBLOCK] = (char) FINALCATA;
		catalogue[randNum][TYPEPOSITION] = CATATYPE;
		catalogue[randNum][PARENTCATA] = (char) NOWCATAPOSITION; // 目录项的父目录为当前目录项位置
		catalogue[randNum][NEXTCATA] = (char) catalogue[NOWCATAPOSITION][FIRSTBLOCK]; // 该目录项为父目录下的邻接目录项的第一个
		catalogue[NOWCATAPOSITION][FIRSTBLOCK] = (char) randNum; // 将父目录项的首个子目录项位置记录为新插入的目录项的位置
		catalogue[NOWCATAPOSITION][SIZEPOSITION]++;

	}

	public void deleteCatalogue(String deleteCataName) // 删除文件夹，运用递归
	{
		FilePosition position = findCataPosition(deleteCataName, CATATYPE);
		if (position.getPrePosition() == NOWCATAPOSITION)
		{
			// 当前目录项下的记录的第一个子目录项位置修改为第一个子目录项的邻接目录项的位置
			catalogue[NOWCATAPOSITION][FIRSTBLOCK] = catalogue[position
					.getNowPosition()][NEXTCATA];
			deleteCatalogue(position.getNowPosition()); // 删除找到的子目录项
		} else
		{
			// 要删除的目录项的前一项和后一项相连接
			catalogue[position.getPrePosition()][NEXTCATA] = catalogue[position
					.getNowPosition()][NEXTCATA];
			// 删除找到的子目录项
			deleteCatalogue(position.getNowPosition());
		}

	}

	// 判断position所指示的目录项名字是否和name一样

	public void createFile(String fileName) // 新建文件
	{
		// 创建的目录项在数组的位置是随机产生的
		int randNum = malloc(CATATYPE);
		// 在catalogue空目录项存储相应的信息
		for (int i = 0; i < fileName.length(); i++)
		{
			catalogue[randNum][i] = fileName.charAt(i);
		}
		catalogue[randNum][FIRSTBLOCK] = (char) FINALBLOCK;
		catalogue[randNum][TYPEPOSITION] = FILETYPE;
		catalogue[randNum][PARENTCATA] = (char) NOWCATAPOSITION; // 目录项的父目录为当前目录项位置
		catalogue[randNum][NEXTCATA] = (char) catalogue[NOWCATAPOSITION][FIRSTBLOCK]; // 该目录项为父目录下的邻接目录项的第一个
		catalogue[NOWCATAPOSITION][FIRSTBLOCK] = (char) randNum; // 将父目录项的首个子目录项位置记录为新插入的目录项的位置
		catalogue[NOWCATAPOSITION][SIZEPOSITION]++; // 父目录项文件大小+1
	}

	public String readFile(String fileName)// 读取文件存储在disk的数据
	{
		String dataString = new String();
		// 查找文件位置
		int CataPosition = findCataPosition(fileName, FILETYPE)
				.getNowPosition();
		int diskPosition = catalogue[CataPosition][FIRSTBLOCK];
		if (diskPosition == FINALBLOCK) // 若文件没有数据，返回空字符串
		{
			String temp = "";
			return temp;
		}
		int a = FAT[diskPosition];
		while ((int) FAT[diskPosition] != FINALBLOCK) // 当fat表记录的数据为最后一块，停止循环
		{
			dataString += String.valueOf(disk[diskPosition]);
			diskPosition = FAT[diskPosition];
		}
		dataString += String.valueOf(disk[diskPosition]);
		return dataString;
	}

	public void writeFile(String fileName, String data) // 向文件写数据
	{
		// 寻找文件位置
		FilePosition filePosition = findCataPosition(fileName, FILETYPE);
		// 删除原有的文件数据
		deleteFile(filePosition.getNowPosition());
		// 所需盘块个数
		int size = (data.length() - 1) / FINALBLOCK + 1;
		int length = 0; // 已经存入的字符数量
		int diskPosition = malloc(DISKTYPE);// 申请空盘块
		FAT[diskPosition] = (char) FINALBLOCK; // 将空盘块修改为已占用
		catalogue[filePosition.getNowPosition()][FIRSTBLOCK] = (char) diskPosition;// 设置文件首盘块地址
		for (int i = 0; i < size - 1; i++)
		{

			for (int j = 0; j < DATASIZE; j++)
			{
				disk[diskPosition][i] = data.charAt(length);
				length++;
			}
			int temp = malloc(DISKTYPE);// 获取下一盘块的地址
			FAT[diskPosition] = (char) temp;// 设置现盘块的下一地址信息
			FAT[temp] = (char) FINALBLOCK;// 将空盘块修改为已占用
			diskPosition = temp;

		}
		// 记录最后一个盘块的信息
		for (int i = 0; length < data.length(); length++, i++)
		{
			disk[diskPosition][i] = data.charAt(length);
		}

	}

	public void deleteFile(String fileName)// 给定文件名，删除当前目录下的文件
	{
		FilePosition position = findCataPosition(fileName, FILETYPE);
		if (position.getPrePosition() == NOWCATAPOSITION)
		{
			// 当前目录项下的记录的第一个子目录项位置修改为第一个子目录项的邻接目录项的位置
			catalogue[NOWCATAPOSITION][FIRSTBLOCK] = catalogue[position
					.getNowPosition()][NEXTCATA];
			deleteFile(position.getNowPosition()); // 删除找到的子目录项
		} else
		{
			// 要删除的目录项的前一项和后一项相连接
			catalogue[position.getPrePosition()][NEXTCATA] = catalogue[position
					.getNowPosition()][NEXTCATA];
			// 删除找到的子目录项
			deleteFile(position.getNowPosition());
		}
	}

	public void enterCata(String cataName)// 进入子目录
	{
		FilePosition filePosition = findCataPosition(cataName, CATATYPE);
		NOWCATAPOSITION = filePosition.getNowPosition();
		nowCata = catalogue[NOWCATAPOSITION];
		path = path + "\\" + cataName;
	}

	public void parentCata()// 返回上级目录
	{
		NOWCATAPOSITION = catalogue[NOWCATAPOSITION][PARENTCATA];
		nowCata = catalogue[NOWCATAPOSITION];
	}

	public int getSonCataSize(int position) // 获取该文件下的子文件数量
	{
		int size = 0;
		position = (int) catalogue[position][FIRSTBLOCK];
		while (position != FINALCATA)
		{
			size++;
			position = catalogue[position][NEXTCATA];
		}
		return size;

	}

	public int getNowCP() // 获取当先目录位置
	{
		return NOWCATAPOSITION;
	}

	public String getCataName(int position)// 获取目录项名称
	{
		String name = new String();
		int i = 0;
		while (catalogue[position][i] != NULL && i < CATANAMESIZE)
		{
			name += catalogue[position][i];
			i++;
		}
		return name;
	}

	public void pathUpdate() // 目录路径更新
	{
		ArrayList<String> cataName = new ArrayList<String>();
		int position = NOWCATAPOSITION;
		while (position != NULL)
		{
			cataName.add(getCataName(position));
			position = catalogue[position][PARENTCATA];
		}
		cataName.add(getCataName(position));
		String cataPath = new String();
		for (int i = 0; i < cataName.size() - 1; i++)
		{
			cataPath += cataName.get(cataName.size() - i - 1);
			cataPath += "\\";
		}
		cataPath += cataName.get(0);

		path = cataPath;

	}

	public int getNextCP(int position) // 获取下一个文件地址
	{
		return (int) catalogue[position][NEXTCATA];
	}

	public int getSonCata(int position)
	{
		return catalogue[position][FIRSTBLOCK];
	}

	public char getType(int position)
	{
		return catalogue[position][TYPEPOSITION];
	}

	private boolean readLocalData() // 读取本地数据，数据不存在则返回false；
	{
		// 获取存储数据的文件
		File dataFile = new File(dataPath);
		if ((dataFile.exists()) && (dataFile.isFile())) // 文件存在，读取数据
		{
			try
			{
				FileInputStream fileInputStream = new FileInputStream(dataFile);
				if (fileInputStream.available() == 0) // 判断文件是否为空
				{
					fileInputStream.close();
					return false;
				}
			} catch (FileNotFoundException e1)
			{
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			} catch (IOException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}

			try
			{
				FileReader reader = new FileReader(dataFile);
				for (int i = 0; i < FINALBLOCK; i++)
				{
					reader.read(disk[i]);
				}
				reader.read(FAT);
				for (int i = 0; i < FINALCATA; i++)
				{
					reader.read(catalogue[i]);
				}
				reader.close();
				return true;
			} catch (FileNotFoundException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				return false;
			} catch (IOException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
				return false;
			}

		} else
		// 文件不存在，创建新文件
		{
			try
			{
				dataFile.createNewFile();
				return false;
			} catch (IOException e)
			{
				return false;
			}
		}

	}

	public void writeData2Local()// 将数据保存到本地
	{
		File dataFile = new File(dataPath);
		if (!dataFile.exists()) // 文件不存在则创建一个新的文件
		{
			try
			{
				dataFile.createNewFile();
			} catch (IOException e)
			{
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		try
		{
			FileWriter writer = new FileWriter(dataFile);
			for (int i = 0; i < FINALBLOCK; i++)
			{
				writer.write(disk[i]);
			}

			writer.write(FAT);
			for (int i = 0; i < FINALCATA; i++)
			{
				writer.write(catalogue[i]);
			}
			writer.close();
		} catch (IOException e)
		{
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

}
