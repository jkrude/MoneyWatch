# MoneyWatch
This project came into existence as most German banks do not offer any form of proper reasoning or visualization of your own financial data. Sadly, due to EU regulations (PDP2) it is difficult for individual developers to directly access financial data. Therefore, you have to import your data manually (download as CMT format) and most banks have their own format.

#### Currently possible
- Categorize your transactions
- Visualize all your expenses based on your categories
- Analyze the trend of expenses over time

#### Planned features
- More financial institutes (currently only Sparkasse is available).
- Use a proper database (with Hibernate) to store previously added transactions.
- Export as image.
- Sankey-Plot for a complete picture (income → expenses).

#### How to use
The project is build using *Java11* with *Maven*.
JFoenix is heavily used which needs some extra permissions as of Java9, they are stated in the maven plugin.
1. Download the source code.
2. Use the maven goals `javafx:compile` and `javafx:run` to start it.
3. Login to your Sparkasse account → Go to "Umseatze" and click on export (Excel(CSV-CMT))

If you use IntelliJ, you may need to mark the *java* folder as project root or right-click on `pom.xml` and import as maven project.

#### Screenshots
<img src="https://user-images.githubusercontent.com/36801164/195415725-41a9e0a3-c338-47ed-96d6-c3b0f1fea158.png" width=750>
<img src="https://user-images.githubusercontent.com/36801164/195415737-5f0104db-a9a7-402f-898c-6e9ab58f5240.png"  width=750>
