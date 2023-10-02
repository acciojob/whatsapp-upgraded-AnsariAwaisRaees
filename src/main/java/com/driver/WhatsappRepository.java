package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below-mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    //HashMap<String,User> userMap = new HashMap<>();


    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String saveUser(String name, String mobile) throws Exception {

        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        User user = new User(name,mobile);
        return "SUCCESS";
        // userMap.put(name,user);
    }



    public Group createGroup(List<User> users) {

        List<User> listOfUser = users;
        User admin = listOfUser.get(0);
        String groupName = "";
        int numberOfParticipants = listOfUser.size();

        if(listOfUser.size() == 2){//the group is a personal chat
            groupName = listOfUser.get(1).getName();
        }
        else{
            this.customGroupCount += 1;
            groupName = "Group " + customGroupCount;
        }

        Group group = new Group(groupName,numberOfParticipants);
        adminMap.put(group,admin);// add admin to the adminMap
        groupUserMap.put(group,users);// add the list of group to the group-user-Map
        return  group;
        //return "group added";
    }

    public int createMessage(String content) {
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        this.messageId += 1;
        Message message = new Message(messageId,content);
        //messageMap.put(messageId,message);// add message to the message Map
        return messageId;
    }


    public int sendMessage(Message message, User sender, Group group) throws Exception{

        if(adminMap.containsKey(group)){
            List<User> users = groupUserMap.get(group);
            Boolean userFound = false;
            for(User user: users){
                if(user.equals(sender)){
                    userFound = true;
                    break;
                }
            }
            if(userFound){
                senderMap.put(message, sender);
                if(groupUserMap.containsKey(group)){
                    if(groupMessageMap.get(group) !=null ){
                        List<Message> messages = groupMessageMap.get(group);// it was giving me null pointer exception

                        messages.add(message);
                        groupMessageMap.put(group, messages);
                        return messages.size();
                    }else{
                        List<Message> newMessage = new ArrayList<>();
                        newMessage.add(message);
                        groupMessageMap.put(group, newMessage);
                        return newMessage.size();
                    }

                }

            }
            throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group does not exist");

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {

        if(groupUserMap.containsKey(group)){
            if(adminMap.containsKey(group)){
                List<User> listOfUser = groupUserMap.get(group);
                if(listOfUser.contains(user)) {

                    adminMap.put(group,user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Approver does not have rights");
        }
        throw new Exception("Group does not exist");
    }

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contain any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group, and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)


        Boolean flag = false;

        List<Message> messageList = new ArrayList<>();
        for (Message message : senderMap.keySet()){
            if (senderMap.get(message).equals(user)){
                messageList.add(message);
            }
        }
        for (Message message : messageList){
            if (senderMap.containsKey(message)){
                senderMap.remove(message);
            }
        }

        Group userGroup = null;

        for(Group group : groupUserMap.keySet()){

            if(adminMap.get(group).equals(user)){
                throw new Exception("Cannot remove admin");
            }
            List<User> userList = groupUserMap.get(group);
            if(userList.contains(user)){
                flag = true;
                userGroup = group;
                userList.remove(user);

            }
        }
        if (flag == false){
            throw new Exception("User not found");
        }

        List<Message> messages = groupMessageMap.get(userGroup);
        for (Message message : messageList){
            if (messages.contains(message)){
                messages.remove(message);
            }
        }

        int ans = groupUserMap.get(userGroup).size() + groupMessageMap.get(userGroup).size() + senderMap.size();
        return ans;
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        //This is a bonus problem and does not contain any marks
        // Find the Kth the latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception

        List<Message> messageList = new ArrayList<>();

        for (Message message : senderMap.keySet()){
            Date time = message.getTimestamp();
            if (start.before(time) && end.after(time)){
                messageList.add(message);
            }
        }
        if (messageList.size() < k){
            throw  new Exception("K is greater than the number of messages");
        }

        Map<Date , Message> hm = new HashMap<>();

        for (Message message : messageList){
            hm.put(message.getTimestamp(),message);
        }

        List<Date> dateList = new ArrayList<>(hm.keySet());

        Collections.sort(dateList, new sortCompare());

        Date date = dateList.get(k-1);
        String ans = hm.get(date).getContent();
        return ans;

    }


    class sortCompare implements Comparator<Date>
    {
        @Override
        // Method of this class
        public int compare(Date a, Date b)
        {
            /* Returns sorted data in Descending order */
            return b.compareTo(a);
        }
    }
}