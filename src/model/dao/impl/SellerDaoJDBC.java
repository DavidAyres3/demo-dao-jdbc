package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao{
	
	private Connection conn;
	
	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public void insert(Seller obj) {
	    PreparedStatement st = null;
	    try {
	        // Preparando o comando SQL para adicionar um novo vendedor à tabela 'Seller'
	        st = conn.prepareStatement("INSERT INTO seller "
	                + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
	                + "VALUES (?, ?, ?, ?, ?)",
	                Statement.RETURN_GENERATED_KEYS);

	        // Definindo os parâmetros para o comando SQL com os dados do vendedor fornecidos
	        st.setString(1, obj.getName());
	        st.setString(2, obj.getEmail());
	        st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
	        st.setDouble(4, obj.getBaseSalary());
	        st.setInt(5, obj.getDepartment().getId());

	        // Executando o comando SQL de inserção
	        int rowsAffected = st.executeUpdate();

	        // Verificando se alguma linha foi afetada (ou seja, se a inserção foi bem-sucedida)
	        if (rowsAffected > 0) {
	            // Obtendo a chave gerada automaticamente pelo banco de dados (no caso, o ID do vendedor)
	            ResultSet rs = st.getGeneratedKeys();
	            if (rs.next()) {
	                // Atribuindo o valor do ID gerado ao objeto Seller
	                int id = rs.getInt(1);
	                obj.setId(id);
	            }
	            // Fechando o ResultSet após uso
	            DB.closeResultSet(rs);
	        } else {
	            // Lançando uma exceção personalizada em caso de erro durante a inserção
	            throw new DbException("Unexpected error! No rows affected.");
	        }
	    } catch (SQLException e) {
	        // Capturando e relançando qualquer exceção SQL como uma exceção personalizada
	        throw new DbException(e.getMessage());
	    } finally {
	        // Certificando-se de que o PreparedStatement seja fechado, independentemente do resultado
	        DB.closeStatement(st);
	    }
	}


	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;
		
		try {
			//Método para atualizar os valores da tabela seller.
			st = conn.prepareStatement("UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
					+ "WHERE Id = ?");
			
	        st.setString(1, obj.getName());
	        st.setString(2, obj.getEmail());
	        st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
	        st.setDouble(4, obj.getBaseSalary());
	        st.setInt(5, obj.getDepartment().getId());
	        st.setInt(6, obj.getId());

			st.executeUpdate();
			
	    } catch (SQLException e) {
	        // Capturando e relançando qualquer exceção SQL como uma exceção personalizada
	        throw new DbException(e.getMessage());
	    } finally {
	        // Certificando-se de que o PreparedStatement seja fechado, independentemente do resultado
	        DB.closeStatement(st);
	    }
	
		
	}

	@Override
	//Método para deletar do banco de dados por id
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("DELETE FROM seller WHERE Id = ?");
			
			st.setInt(1, id);
			
			int arrowsAffected = st.executeUpdate();
			
			if(arrowsAffected == 0) {
				throw new DbException("Usuário não encontrado.");
			}
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//Consulta SQL que procura um seller que tenha o mesmo id nas tabelas Seller e Department. 
			st = conn.prepareStatement(""
					+ "SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE seller.Id = ?");
			
			st.setInt(1, id);
			
			//Executa a consulta e guarda seu resultado no ResultSet rs.
			rs = st.executeQuery();
			
			// Se houver uma linha de resultado, o rs.next() avançará para essa linha e retornará true.
			//Instancia um objeto Department e um objeto Seller usando os dados da linha atual.
			if(rs.next()) {
				Department dep = instantiateDepartment(rs);
				Seller seller = instantiateSeller(rs, dep);
				return seller;
			}
			
			return null;
			
			
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	//Método que instancia um Seller utilizando os dados de um Seller passados para o ResultSet rs.
	//E o dep do método instantiateDepartment.
	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException{
		Seller seller = new Seller();
		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		seller.setBirthDate(rs.getDate("BirthDate"));
		seller.setDepartment(dep);
		return seller;
	}

	//Método que instancia um Department utilizando os dados passados para o rs
	private Department instantiateDepartment(ResultSet rs) throws SQLException{
		Department dep = new Department();
		dep.setId(rs.getInt("DepartmentId"));
		dep.setName(rs.getString("DepName"));
		return dep;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			//Comando SQL para selecionar todas as linhas da tabela seller + department
			//que possuem nas duas tabelas o departmentId igual.
			st = conn.prepareStatement(""
					+ "SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "ORDER BY Name");
			
			//executa a consulta
			rs = st.executeQuery();
			
			//instancia uma lista de Seller
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			while(rs.next()) {
				Department dep = map.get(rs.getInt("DepartmentId"));
				//Percorre o DepartmentId com o map, verificando se o mesmo possui valores.
				//Se não obtiver resultado, entra no if
				
				if(dep == null ) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				//Se já existir um id correspondente em Department, utiliza o valor existente.
				//Evitando a criação duplicada de objetos Department para o mesmo id.
				Seller seller = instantiateSeller(rs, dep);
				list.add(seller);
				
			}
			return list;
			
			
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		
	}


	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			// Comando SQL para selecionar todas as linhas da tabela department 
			// que possuem um DepartmentId específico.
			st = conn.prepareStatement(""
					+ "SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE DepartmentId = ? "
					+ "ORDER BY Name");
			
			st.setInt(1, department.getId());
			
			//executa a consulta
			rs = st.executeQuery();
			
			//instancia uma lista de Seller
			List<Seller> list = new ArrayList<>();
			Map<Integer, Department> map = new HashMap<>();
			
			
			while(rs.next()) {
			
				Department dep = map.get(rs.getInt("DepartmentId"));
				//Percorre o DepartmentId com o map, verificando se o mesmo possui valores.
				//Se não obtiver resultado, entra no if
				if(dep == null ) {
					dep = instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				//Se já existir um id correspondente em Department, utiliza o valor existente.
				//Evitando a criação duplicada de objetos Department para o mesmo id.
				Seller seller = instantiateSeller(rs, dep);
				list.add(seller);
				
			}
			return list;
			
			
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		
	}

}
