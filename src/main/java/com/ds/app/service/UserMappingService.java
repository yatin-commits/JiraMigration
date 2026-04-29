package com.ds.app.service;

import com.ds.app.entity.JiraUser;
import com.ds.app.repository.JiraUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final JiraUserRepository userRepository;

    // Hardcoded mapping from the sheet provided
    private static final Map<String, String> USER_MAP = new HashMap<>();

    static {
        USER_MAP.put("63edf23dd08a73e538f49cab", "Aqueel Ahmad Khan");
        USER_MAP.put("63edf23d333d0e2ec16f5b49", "Manisha Prajapati");
        USER_MAP.put("641417fe7222b08f3e717530", "mayuri.patel");
        USER_MAP.put("6412f5dc5534b0bf744076fe", "dilip.bakshi");
        USER_MAP.put("6412bc59c35660c269ba7031", "sunil.kumar");
        USER_MAP.put("622700b48a4bb60068f4e8f5", "Gorav Pal");
        USER_MAP.put("557058:8341daf3-3bcd-4e6c-a656-1ca9bae948ca", "vijay_kott");
        USER_MAP.put("6422938f9796ea0a87192b22", "Amal PS");
        USER_MAP.put("712020:a51a6a1b-5e4b-433d-a5e4-001e731ad38b", "sanjay yadav");
        USER_MAP.put("712020:afef2716-ef00-4c4b-b7f6-bca2cde6be06", "Usman Chaudhry");
        USER_MAP.put("63b8351c6ad11358a09abf1b", "kirt.chana");
        USER_MAP.put("641417fe2cee496759eb05e8", "Madhavi Avvaru");
        USER_MAP.put("63c69b03d16a886a25e60ea3", "Sayida Najm");
        USER_MAP.put("64199c5a407493675d46f32f", "Rahul Sharma QA");
        USER_MAP.put("640aaa45896d10ebd474c833", "Gerry Butler");
        USER_MAP.put("640abad615d668edd8f1994b", "Danny Chavis");
        USER_MAP.put("712020:50c14157-e649-4f1e-b477-3fbe3fb9b042", "Aditya Singh");
        USER_MAP.put("64229455c35660c269bc8473", "Nithya Unnikrishnan");
        USER_MAP.put("5ef36bb61cd0440ab6b4e59e", "Divya Dsouza");
        USER_MAP.put("63fef72a0a4a47fb8d216414", "Sahir Jafri");
        USER_MAP.put("642293f6c35660c269bc8457", "Jobin Joseph");
        USER_MAP.put("557058:a58b2c04-0fc8-4c26-8794-f13d7daa61db", "Sreeni Janapati");
        USER_MAP.put("70121:6f290432-1619-4637-9edf-e8d71c93b289", "Raj S J Kott");
        USER_MAP.put("712020:ac305df3-844e-411d-b28c-17daddb85b6c", "Brendan Chavis");
        USER_MAP.put("642294556b29c052ab2eeb70", "Steeve Issac Saju");
        USER_MAP.put("712020:18e919bd-7745-4a23-833e-d0860212938b", "Rajeev Malhotra");
        USER_MAP.put("640abad6896d10ebd474cc11", "brad.patton");
        USER_MAP.put("712020:140498b7-5bdd-451a-b385-7cbdb301cf3f", "Pranjal Yadav");
        USER_MAP.put("6412bc599796ea0a87171686", "Sandeep Dabral");
        USER_MAP.put("605ac49a0a9b6900719f742b", "Manasi Koshe");
        USER_MAP.put("712020:b7d5e68a-e24f-47a0-98b0-3b7163eecb9f", "sandeep singh papola");
        USER_MAP.put("63fef72a9ce2cd2c240dbb0f", "ijaz.khan");
        USER_MAP.put("712020:98aa9785-6e45-4823-a699-f544020af2c3", "Amit Pratap Singh");
        USER_MAP.put("712020:222297f9-0d86-44c5-8449-170a5815f1bd", "Rohit Verma");
        USER_MAP.put("712020:a03c81f9-7c83-4f85-ba14-7fa0ec481e06", "Mayank Khurana");
        USER_MAP.put("63d38eae8dd199a03e11f1d9", "Akhilesh Singh");
        USER_MAP.put("64141857f1b529dfa98c8d93", "david.pagolu");
        USER_MAP.put("712020:d597a135-f6f3-4b1a-a307-cf3ecb7d0222", "Nikita Sumant");
        USER_MAP.put("63636a22a04e906250c99ed7", "harshit.aggarwal");
        USER_MAP.put("70121:f6cf5771-cea9-44fe-bf3f-ee71f240ea98", "Prathap Komera");
        USER_MAP.put("642293f6b05b4e3e7dab65d2", "Neha Thomas");
        USER_MAP.put("6422938f407493675d480fa6", "Ahamed Riswan");
        USER_MAP.put("64268c8faf3b93d8ecf4dbfd", "Saurabh Raybaruah");
        USER_MAP.put("6422947f0e6828ab2026fe35", "Suraj Vv");
        USER_MAP.put("64141cebaf3b93d8ecf24dc9", "asha.bhatnagar");
        USER_MAP.put("6046579b0b784000690de406", "Rohit Manhas");
        USER_MAP.put("712020:1abdb991-0075-4a1c-835d-cf41ec6b289e", "Ajay Verma");
        USER_MAP.put("60b0d7ca55c9b2006f0f49bc", "Rohit Kumar");
        USER_MAP.put("642293f61273131f2ae307c6", "Jasna");
        USER_MAP.put("6047ba61b2ea1201159819f8", "Rakesh Allahabadi");
        USER_MAP.put("63809f95de5cdaba3a6813e4", "Subarshini Ramesh");
        USER_MAP.put("557058:756337ee-9a78-432d-8bf6-670ce2732b48", "Saurabh Arora");
        USER_MAP.put("642293f69796ea0a87192b55", "Joseph Roy");
        USER_MAP.put("63635a29b0b6ef03564b4f58", "Seemanki Aggarwal");
        USER_MAP.put("64144fae9d2bc6c90a8a389b", "Sonu Kumar");
        USER_MAP.put("63e0b242fb75f8568f6242e7", "Sanjeev kumar");
        USER_MAP.put("712020:eab19a0e-63fd-4609-87b7-91c7448b0c94", "pankaj.sharma");
        USER_MAP.put("633286e38b75455be4562471", "Santosh kumar");
        USER_MAP.put("712020:de587b0e-9257-4987-8cab-baa4053a684d", "pratik.patil");
        USER_MAP.put("712020:8a79b851-37f7-43d9-ba99-667231b2cfc5", "Aman");
        USER_MAP.put("712020:40dc728a-32c9-40d0-ae3d-914fc3f9d0ab", "Rohit Gupta");
        USER_MAP.put("63329ca3409249995ee9b3e7", "Shipra");
        USER_MAP.put("640091f915d668edd8ede06a", "Vidhi Mathur");
        USER_MAP.put("61714e51892c42007211373b", "Devanand Metangale");
        USER_MAP.put("63a04fdf6ad11358a09631d6", "AHMED SAEED");
        USER_MAP.put("63352ea07f85f16777a05b3b", "Ajit");
        USER_MAP.put("712020:7a2f2973-14f9-447e-a786-67db0cefcd5b", "Shagun Minhas");
        USER_MAP.put("63fc67dd2847866310fda758", "sharukh.shaikh");
        USER_MAP.put("61710b58bcb5740068249807", "Aparna Kaushik");
        USER_MAP.put("5f464ac71ac29c004520959d", "Pradeep Chauhan");
        USER_MAP.put("62b14ab9b8e499e567e8a7f1", "AnkitRathore");
        USER_MAP.put("5e900c72dc19850b87357b9c", "Deepak Chauhan");
        USER_MAP.put("64005d28feb4b150c576e888", "tai.osunsan");
        USER_MAP.put("640061df2847866310fe9f44", "Anjani Maurya");
        USER_MAP.put("641c26aa0e6828ab20266acd", "Rohan Dwivedi");
        USER_MAP.put("61af5f23c15977006a305c52", "Afsha Hamid");
        USER_MAP.put("640ae6130d9b61193c28b48c", "Shaivi Chauhan");
        USER_MAP.put("64141ca57222b08f3e7175b7", "Zeydy Ellis");
        USER_MAP.put("64141d2a6b29c052ab2d134c", "Robert Lewis");
        USER_MAP.put("603c7777ee1177007042b318", "Rocky Joseph");
        USER_MAP.put("642293f62cee496759ecd5c3", "Joseph Mathew");
        USER_MAP.put("712020:9b68eca9-cf53-45d6-a53c-1af1b5fdbc5c", "Kamlesh Munshi");
        USER_MAP.put("712020:989ea23e-bcf9-43e3-ae82-79c4a94ff201", "Prabhjot Kaur");
        USER_MAP.put("64005caa15d668edd8edd18f", "Veeraj Bhatnagar");
        USER_MAP.put("64141d29b05b4e3e7da991bc", "oliver.sloman");
        USER_MAP.put("642293f64b23217e558e0794", "Lidin T");
        USER_MAP.put("64141857c35660c269baac46", "Vinay Chanchlani");
        USER_MAP.put("712020:5a5dceae-c7aa-4ace-a32b-8f54b29d1f27", "Yomi Oyekola");
        USER_MAP.put("712020:89dbe0aa-eb5b-4dd3-bfb8-a6107291fb23", "Jim Casey");
        USER_MAP.put("64141d759796ea0a87175464", "Yugandhar Madireddy");
        USER_MAP.put("64141ceb67102fc717c014b2", "ghanshyam.desai");
        USER_MAP.put("640abad67655a3223a261e89", "nic.nelson");
        USER_MAP.put("63636a22fe5ff375235c03d9", "Shanker Singh");
        USER_MAP.put("712020:68b66815-18ee-46e1-bf4c-3e3142d8d781", "Amisha");
        USER_MAP.put("640ae6137655a3223a262a21", "Vaibhav Kumar Gupta");
        USER_MAP.put("640061df0d9b61193c24dd00", "Pooja");
        USER_MAP.put("712020:e95ad812-0c70-4dd5-8f4d-883eb82a2f2f", "Ayush Kumar");
        USER_MAP.put("642294552cee496759ecd622", "Robin Paul K");
        USER_MAP.put("642293f61273131f2ae307c7", "Neeraj.nair");
        USER_MAP.put("638091219341d1f13605ef5f", "Vijayakanth Kotts");
        USER_MAP.put("712020:6596c979-a16d-4e77-892e-230a9c44ca02", "JEFF GOODRICH");
        USER_MAP.put("6419a55567102fc717c0d13b", "Gaurav Haware");
        USER_MAP.put("641aa5bb9d2bc6c90a8b1ea8", "Rakesh Roushan");
        USER_MAP.put("641418576b29c052ab2d12a3", "simran.sethi");
        USER_MAP.put("641c20db2cee496759ec4354", "Saddam Nadaf");
        USER_MAP.put("6412c16ac35660c269ba70dd", "Manish Agarwal");
        USER_MAP.put("6422938f6b29c052ab2eeb09", "Gopu S Kumar(Kott)");
        USER_MAP.put("641c1fcf67102fc717c156be", "Maheswara Deverapalli");
        USER_MAP.put("712020:155bd041-8ab2-4639-bc88-c3128c1ab07e", "Anju");
        USER_MAP.put("6201f9759493720070b7e58a", "priyanka");
        USER_MAP.put("63edf23dc5061c632c0da95b", "Sunil Rayaguru");
        USER_MAP.put("712020:f6ca5d26-1b48-400f-bf70-c2db0ea92291", "Ankit Nain");
        USER_MAP.put("63fef72a15d668edd8ed6e4c", "Rachana Sapru");
        USER_MAP.put("5c51a1ac1bed9a195187a3f6", "Gaurav Kumar Jain");
        USER_MAP.put("712020:b8051060-38c9-45c1-aa30-216629a0a702", "Uday Singh");
        USER_MAP.put("557058:e366083b-1a4a-4da0-acab-d30c73a68338", "Kripanshu Taunk");
        USER_MAP.put("642294550152b5f4f9f2aadc", "Shyam Mohan T.M");
        USER_MAP.put("712020:08297bc0-e5f3-4dfa-93f1-b303a27fd8dc", "Bharat Gupta");
        USER_MAP.put("6422938faf3b93d8ecf42354", "Ameena VS");
        USER_MAP.put("63636a2213f37118d7288455", "nadeem.qaiser");
        USER_MAP.put("63bba1602341bff4fff5e1a3", "Sayan Sengupta");
        USER_MAP.put("712020:31f47037-d578-40eb-b684-b3eb05189584", "Piyush Bhardwaj");
        USER_MAP.put("63a04fdf030d706ab0e1eda7", "Ayush Agrawal");
        USER_MAP.put("712020:c02884c3-f2c4-4042-aeaa-46086e4ec575", "Nishant Chauhan");
        USER_MAP.put("712020:01e7f9b7-581f-45fa-99c1-8672ae67745c", "Mohan Rawat");
        USER_MAP.put("640ae61381de11a1adfdbdd3", "Simranpreet Kaur");
        USER_MAP.put("6399b3e17145571a7ea82e7f", "Prashant Mokashi");
        USER_MAP.put("712020:0eeed48e-2494-4254-b894-44843b749b5b", "Jasleen Kaur");
        USER_MAP.put("640995cc4d5e4b44e2353a11", "Chitra Karakoti");
        USER_MAP.put("640061df896d10ebd470ff26", "AMAR MISHRA");
        USER_MAP.put("712020:4e9b2a6e-1650-4fab-a0c9-6bb96f1cf435", "Abhiuday Bhushan");
        USER_MAP.put("712020:2550c970-3c34-4283-a384-cc95c2ebc971", "Shikha Awasthi");
        USER_MAP.put("6412eab05534b0bf74407483", "Satendra Kumar");
        USER_MAP.put("6422938f2cee496759ecd594", "C.S Divya");
        USER_MAP.put("712020:cd721703-70be-48af-a76c-66fffb56a8a8", "Jhanvi Mundra");
        USER_MAP.put("642294557222b08f3e73471e", "Reshma.rl");
        USER_MAP.put("712020:99e7baac-6d72-486b-be43-a4462e07cf47", "Navneet Patel");
        USER_MAP.put("712020:1a2a4fbf-8140-4798-aeb4-b6e8b0ceb1b0", "Rishabh Hooda");
        USER_MAP.put("712020:27f1da83-913a-4e2e-b7cf-0f2c33ca5844", "Sanjeev Kumar Saini");
        USER_MAP.put("640ae6132847866310026f08", "Deepak Pandit");
        USER_MAP.put("640ae61315d668edd8f1a5b1", "Spardha Kumari");
        USER_MAP.put("712020:91612964-c454-4757-99b0-e9610c3bf424", "dev.kashyap");
        USER_MAP.put("6422947faf3b93d8ecf423e4", "Mamatha K P");
        USER_MAP.put("642293f60e6828ab2026fdde", "HEERA M H");
        USER_MAP.put("6412c5637222b08f3e713a7c", "Prabhat Verma");
        USER_MAP.put("6412bc829d6383e32a319059", "Mani Singh");
        USER_MAP.put("62ceae55e546e8eab8eeb035", "Anil doke.ext");
        USER_MAP.put("6421175a67102fc717c1ce36", "rahul.kumar");
        USER_MAP.put("712020:0d5c3ada-d8fb-4c87-879d-c5b4423fe55b", "Jyoti Jayswal");
        USER_MAP.put("712020:0ae5f770-6959-4173-a713-1dbdef4778db", "niraj.kumar");
        USER_MAP.put("712020:2bde6162-a291-4393-9a48-d0680fecc4a6", "Ishan Kumar Sahu");
        USER_MAP.put("642294550e6828ab2026fe1a", "shibina p");
        USER_MAP.put("712020:0067b9e9-dfca-4db1-867e-39eb2b24e21a", "Sahil Godwal");
        USER_MAP.put("5f9129f829bd8a006f7c3c10", "Naresh Malaviya");
        USER_MAP.put("6412ee715534b0bf74407537", "aman.kumar");
        USER_MAP.put("63a04fdf082abdd71bb2ed61", "Tushar Patole");
        USER_MAP.put("6422938fb05b4e3e7dab6573", "Athira Subrahmanian");
        USER_MAP.put("712020:e502b08f-4d41-466b-a257-fc50a78e91cb", "Elizabath Chacko");
        USER_MAP.put("712020:bc8fe585-89b8-41b9-9eab-b117d4b2365b", "Bhanu Priya");
        USER_MAP.put("6422938f7222b08f3e73468f", "Athira CP");
        USER_MAP.put("642293f65534b0bf74427ed0", "jerin.majeed");
        USER_MAP.put("63edf23d5fa5d13d1e11e62a", "Shyam Mohan T.M");
        USER_MAP.put("6194ff36b0b630006a600b09", "Syeda Mehreen");
        USER_MAP.put("620440808df9a200718e9b65", "Jagdish Prasad");
        USER_MAP.put("6426a204407493675d48c95c", "dennis.lam");
        USER_MAP.put("641da2db1273131f2ae2b132", "Anula Borole");
        USER_MAP.put("640b1925feb4b150c57ad055", "Prathamesh Patil");
        USER_MAP.put("640b19250d9b61193c28c802", "Pratiksha Bombarde");
        USER_MAP.put("6178f2d9860f78006b345aec", "vishal vairat");
        USER_MAP.put("640b1925284786631002832a", "Vidhi Doshi");
        USER_MAP.put("633148df748d1bfcb858eabf", "Vaibhav Lambey");
        USER_MAP.put("6422d8b00e6828ab202713ba", "Pradip Bhoi");
        USER_MAP.put("615b0363d9820f0070a0f773", "Vaibhav Kulkarni");
        USER_MAP.put("712020:100c6ef0-851d-44ef-93d9-32c4d320d4ed", "Udit Mehra");
        USER_MAP.put("712020:3c593c98-ef8b-494e-a63c-c50c4ade023e", "Former user");
        USER_MAP.put("6421176caf3b93d8ecf406af", "Rahul Sharma Data");
        USER_MAP.put("712020:f9119852-c718-4425-9544-957e336b7bed", "sachin tyagi");
        USER_MAP.put("64141cc70152b5f4f9f0d1bc", "Anoja Wijesekera");
        USER_MAP.put("712020:04a4b39d-d2da-483d-b284-8eeac773daeb", "Priyanka Chavan");
        USER_MAP.put("6412c16a9d2bc6c90a89f20a", "Rajan Kumar Mishra");
        USER_MAP.put("712020:53f8d34f-879e-442d-8996-f1c4768bf5ae", "Former user");
        USER_MAP.put("712020:a708dbc9-43b9-4c24-a3e6-47d4c5502394", "bryce.potzer");
        USER_MAP.put("712020:43c463a5-3885-44af-9d2e-02986fc4b9bd", "rahul.jolly");
        USER_MAP.put("712020:3616f4a8-4646-4072-b6b1-b8113f1b13f8", "vivek.gaur");
        USER_MAP.put("557058:c5acffa4-7aba-456a-b624-2dd3ad6cb384", "mukesh.shaw@ocr-inc.com");
        USER_MAP.put("712020:1e04a90a-445d-4f98-95a2-24c7ebe61ec4", "Elayne Garber");
        USER_MAP.put("557058:8898a909-14c1-4126-b248-a3670f9a8b0b", "Sanat Kumar");
        USER_MAP.put("642a976422330bdf97ab2a0f", "shahbaz.ahmad");
        USER_MAP.put("5dfcda9abe20390cb348159b", "Raghav Ahuja");
        USER_MAP.put("641417fec35660c269baac31", "Sudhir Bhatnagar");
        USER_MAP.put("712020:7b5fb6c2-7018-409f-b5c8-459af48772bf", "rachana.rathi");
        USER_MAP.put("712020:af37b92b-a245-4ea4-bf55-b02d16004010", "bruce.moore");
        USER_MAP.put("63edf23d7e8114bf5b2729b5", "Subarshini Ramesh");
        USER_MAP.put("608c2f59c210c1006ce9a5fb", "Abhishek Kishore");
        USER_MAP.put("642abb2722330bdf97ab2d84", "chavis.danny");
        USER_MAP.put("712020:2269ba82-4a6d-4611-9239-9e77f00102cc", "imran.mustapha");
        USER_MAP.put("63c69b032c6573abb08cd430", "Saroj");
        USER_MAP.put("712020:36e40866-f940-42fd-a730-d30ebf6f8097", "Sreelekha.K");
    }

    public String resolveName(String userId) {
        if (userId == null || userId.isBlank()) return "";
        return USER_MAP.getOrDefault(userId.trim(), userId.trim()); // fallback to userId if not found
    }

    public Map<String, String> getAllMappings() {
        return USER_MAP;
    }

    public void saveAllToDb() {
        USER_MAP.forEach((id, name) ->
            userRepository.save(JiraUser.builder().userId(id).userName(name).build())
        );
        log.info("Saved {} users to DB", USER_MAP.size());
    }
}