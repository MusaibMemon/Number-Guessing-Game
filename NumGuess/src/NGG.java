import java.sql.*;
import java.util.*;

//Database connection
class DatabaseConnection {
    static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/numgg";
        String username = "root";
        String password = "";
        return DriverManager.getConnection(url, username, password);
    }
}

//Main Class
class NGG {
    public static void main(String[] arg) {
        Scanner sc = new Scanner(System.in);
		GuessNumber g1 = new GuessNumber();
        Player.loadUsersFromDatabase();
		String play = "ply";

        while (!play.equals("c")) {
            System.out.println();
            System.out.println("Press N for new player");
            System.out.println("Press P for existing player");
            System.out.println("Press C to close");
            System.out.println();
            System.out.print("Enter your choice : ");
			play = sc.next().toLowerCase();

			// adding new user to list
            if (play.equals("n")) {
                System.out.print("Enter user name: ");
                String name = sc.next();

                //If user already exist in database
                while (Player.userExists(name)) {
                    System.out.print("User name already exist! try another : ");
					name = sc.next();
                }

                Player.registerUser(name);
                // ...
            } else if (play.equals("p")) {
                System.out.print("Enter existing user name : ");
				String name = sc.next();

                //checks user name already exist than start playing
                if (!Player.userExists(name)) {
                    System.out.println("User does not exist.");
                } else {
                    String playAgain = "";
					Player p = Player.userExist(name);
					System.out.println("Your high score was = "+p.score);
					
					while (!playAgain.equals("c")) {
						
						//update Score for player
                        p.score += g1.getData(); // Update the player's score in the program
                        Player.updateScore(name, p.score); // Update the player's score in the database
                        System.out.println("Your score = " + p.score);

                        System.out.print("Do you  want to play again (y/n) : ");
                        String pA = sc.next().toLowerCase();
                        if(pA.equals("n")){
                            break;
                        }
	
					}
                }
            }
        }
    }
}

class Player {
    String name;
    int score=0;
    
    static ArrayList<Player> list = new ArrayList<>();
	//check id=f user exists
    static Player userExist(String n) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(n)) {
                return list.get(i);
            }
        }
        return null;
    }
    
    // Register a new user in the database
    static void registerUser(String name) {
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (name, score) VALUES (?, 0)")) {
            statement.setString(1, name);
            statement.executeUpdate();

            // Create a new Player instance and add it to the list
            Player player = new Player();
            player.name = name;
            player.score = 0;
            list.add(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void loadUsersFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {
            list.clear(); // Clear the list
            while (resultSet.next()) {
                Player player = new Player();
                player.name = resultSet.getString("name");
                player.score = resultSet.getInt("score");
                list.add(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Check if user exists in the database
    static boolean userExists(String name) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Load user data from the database
    static Player loadUser(String name) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Player player = new Player();
                    player.name = name;
                    player.score = resultSet.getInt("score");
                    list.add(player); // Add the loaded player to the list
                    return player;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update user's score in the database
    static void updateScore(String name, int newScore) {
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE users SET score = ? WHERE name = ?")) {
            statement.setInt(1, newScore);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


// Game
class GuessNumber {
	Scanner sc = new Scanner(System.in);
	int getData(){
		// compiler takes random number between 1 to 100
		int number = 1 + (int)(100* Math.random());
		
		System.out.println("\nYou have only 10 trials....Best of luck\n");
		int i; int score=10;
		
		//give 10 trials to user and everytime user input wrong ans than score will decreased by 1
		for(i=0; i<10; i++,score--){
			System.out.print("Enter the number between 1-100 :");
			int guess = sc.nextInt();
			if(number==guess){
				System.out.println("\nCongratulations! You guessed the number.\n");
				break;
			}
			
			// if number is low than gives hint 
		    else if(guess>number){
			    System.out.println("Number is High\n");
		    }
			
			// if number is high than gives hint
		    else{
			    System.out.println("Number is Low\n");
		    }
		}
		
		// after 10 wrong trials it will show number guessed by pc
		if (i == 10) {
            System.out.println("You loosed the game");
            System.out.println("The number was "+number);
        }
		return score;
	}	
}

