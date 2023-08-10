package application;

import java.util.List;
import java.util.Scanner;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class Program2 {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		DepartmentDao departmentDao = DaoFactory.createDepartmentDao();
		
		//System.out.println("=== TEST 1: department insert");
		//departmentDao.insert(dep);
		//System.out.println(dep.toString());

		
		/*
		System.out.println("=== TEST 2: department update");
		Department dep = departmentDao.findById(10);
		dep.setName("Frios");
		departmentDao.update(dep);
		System.out.println(dep.toString());
		*/
		
		/*
		System.out.println("=== TEST 3: department delete");
		System.out.println("Enter an id to delete");
		int id = sc.nextInt();
		departmentDao.deleteById(id);
		System.out.println("Deleted department of id " + id);
		*/
		
		/*
		System.out.println("=== TEST 4: department find all departments");
		List<Department> list = departmentDao.findAll();
		for(Department d : list ) {
			System.out.println(d);
		}
		*/
		
		/*
		System.out.println("=== TEST 4: department find department by id");
		Department dep = departmentDao.findById(10);
		System.out.println(dep);
		*/
		
	}
}
