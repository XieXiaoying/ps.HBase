package psensing;
import Trajectory.TrajTable;
import java.sql.*;
import java.util.*;

public class TrajPre {
	public Mysql users = new Mysql();
	
	public ArrayList<List<String>> findTraj()
	{
		
		TrajTable trajTable = new TrajTable();
		ArrayList<String> user_id = users.get_users();
		ArrayList<List<String>> allTrajs = new ArrayList<List<String>>();
	//	List<String> each_grids = new ArrayList<String>();
		List<String> infos = new ArrayList<String>();
		System.out.println("users:"+user_id);
		
		for(String tmp: user_id)
		{
			System.out.println(tmp);
			List<String> trajs = new ArrayList<String>();
			trajs = trajTable.getTrajIds(tmp,null);
			System.out.println("user_traj:"+trajs);
			
			for(String traj: trajs)
			{
				//System.out.println("aaaaaaa");
				System.out.println("guiji_id:"+traj);
				infos = null;
				infos = trajTable.getPath(tmp, traj, null);
			//	System.out.println("grids:"+infos);
				List<String> each_grids = new ArrayList<String>();	//one each_grids is related to a traj

				for(String info: infos)
				{
				//	System.out.println(info);
					String[] arr = info.split("/");
					String grid = arr[0];
					//String time = arr[1];
				//	System.out.println(grid);
					each_grids.add(grid);	
				}
				System.out.println(each_grids);
				allTrajs.add(each_grids);
				System.out.println(allTrajs);
			}
		}
		return allTrajs;
	}
	public static void main(String[] args) 
	{
		TrajPre test = new TrajPre();
		ArrayList<List<String>> brr = test.findTraj();
		
		//String[][] test1 = (String[][])brr.toArray(new String[0][0]);
		
		//for(List<String> list: brr)
		//{
		//	System.out.println(brr);
		//}
	}
}
