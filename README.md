FaaChain v2.0


FaaChain 飞天公链是一个使用 Java 语言构建的联盟组织形式的区块链底层项目。

使用拜占庭容错共识机制，出块速度高达2-5秒/块，支持并发处理超大数量的交易请求。


FaaChain联盟组织由一个 MainNode + 1-20个 DevNode 组成；

MainNode和DevNode都具有相同的出块挖矿功能；

MainNode具有维持联盟关系的功能；

FaaChain自带区块浏览器Web模块，建议在MainNode上使用；

认同同一个MainNode的DevNode会和该MainNode组成一个区块链联盟，进行相应的区块链业务。不同的MainNode联盟之间互不干扰，视为FaaChain的不同分叉；


FaaChain出块奖励RoadMap：

预挖社区推广奖励：7,000,000枚FAA

正式挖矿爆块奖励：6枚FAA

第一次减半时间在主网运行4年后，高度为：13,416,666

在经历20次减半后，爆块奖励趋近于0，FAA总币数控制在168,000,000枚

具体代码：

    public static double[] REWARD_ROAD_A = new double[]{
            80500000,
            120750000,
            140875000,
            150937500,
            155968750,
            158484375,
            159742187,
            160371093,
            160685546,
            160842773,
            160921386,
            160960693,
            160980346,
            160990173,
            160995086,
            160997543,
            160998771,
            160999385,
            160999692,
            161000000
    };
    public static double[] REWARD_ROAD_B = new double[]{
            6,
            3,
            1.5,
            0.75,
            0.375,
            0.1875,
            0.09375,
            0.04687,
            0.02343,
            0.01171,
            0.00585,
            0.00292,
            0.00146,
            0.00073,
            0.00036,
            0.00018,
            0.00009,
            0.00004,
            0.00002,
            0.00001
    };


FaaChain Api：

交易结果查询：
GET请求：/faaGetTransactionReceipt?hash=fxffe0b0081862b9501d88038a6aa72b4e4a7bfefbb6e7f191cbd98c7ec66b31be
参数hash为交易哈希

结果：{"err":0,"status":9,"msg":"success"} 
err为请求结果，0表示请求成功
status为交易状态，9为成功、-2为无此交易、-1为失败、0为pending
msg为返回的文字消息


单条交易详情查询：
GET请求：/faaGetTransactionDetail?hash=fx95dc0dac31646493cb5cb831fe1004ddcfb1df056d70955f370e9d7caca2157f
参数hash为交易哈希

结果：{"dateCreated":1584674779000,"blockNo":16946,"fee":"1860000000000000000","fromAddress":"FW353EDC58FBCE2CDD01D937868BE5AC9D519B251299","toAddress":"FW3CF9E4F8C13B0C8EBEA3393BD03E707415BDA75EB8","value":"1200000000000000000","hash":"fx95dc0dac31646493cb5cb831fe1004ddcfb1df056d70955f370e9d7caca2157f","hexdata":"0xf88395353edc58fbce2cdd01d937868be5ac9d519b251299953cf9e4f8c13b0c8ebea3393bd03e707415bda75eb889057c6e9fd1c58800008819d00c25323a0000801ca0e14ff50a84f7699749a722355062fef78258d65548c0e9727929b1b92404377ba00ea7b54099bac839fc7b960dec3cce5a36d349753dc17dc61dba845a86f225df","status":9}
status为交易状态，9为成功、-2为无此交易、-1为失败、0为pending


钱包余额查询：
GET请求：/faaGetBalance?address=FW52C07C5DF30A810D89866308E7CA973C1974DCD92F
参数address为钱包地址

结果：{"err":0,"balance":865010000000000000000,"balance_human":865.010000000000000000}
err为请求结果，0表示请求成功
balance为最小单位余额（FAA小数18位）
balance_human为基准单位余额（个）


获取当前推荐手续费：
GET请求：/faaGetFeeRecommend

结果：{"err":0,"fee":1.108}
err为请求结果，0表示请求成功
fee为建议的手续费，基准单位（个）


获取当前最新的区块号：
GET请求：/faaLastBlockNumber

结果：{"err":0,"number":157}
err为请求结果，0表示请求成功
number为最新区块号


获取交易所中的当前美元价格：
GET请求：/faaGetPrice

结果：{"err":0,"price":0.3868}
err为请求结果，0表示请求成功
price为当前价格，单位美元


遍历区块交易列表（用于判断充值）：
GET请求：/faaGetLogs?fromBlock=26&toBlock=108
参数fromBlock为查询起始区块号
参数toBlock为查询结束区块号
注意，单次查询最大跨度为100个区块

结果：
{"err":0,
"data":
	[
	{"dateCreated":1582350047000,"blockNo":101,"fee":"1200000000000000000","fromAddress":"FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9","toAddress":"FW52C07C5DF30A810D89866308E7CA973C1974DCD92F","value":"160000000000000000000","hash":"fxffe0b0081862b9501d88038a6aa72b4e4a7bfefbb6e7f191cbd98c7ec66b31be","status":9},
	{"dateCreated":1582349615000,"blockNo":66,"fee":"1200000000000000000","fromAddress":"FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9","toAddress":"FW52C07C5DF30A810D89866308E7CA973C1974DCD92F","value":"500000000000000000000","hash":"fx2415ae585eeeca0cb325e10c7d454f4c56869a1ee2ad4de2d97026d991c2f7f6","status":9},
	{"dateCreated":1582349607000,"blockNo":65,"fee":"1300000000000000000","fromAddress":"FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9","toAddress":"FW52C07C5DF30A810D89866308E7CA973C1974DCD92F","value":"100000000000000000000","hash":"fx5a844d1a979c25723438f1fde512d197e4009f363d51f0da7f8e7261bddb349d","status":9},
	{"dateCreated":1582349544000,"blockNo":59,"fee":"1260000000000000000","fromAddress":"FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9","toAddress":"FW52C07C5DF30A810D89866308E7CA973C1974DCD92F","value":"0","hash":"fxc27b46409aa7f99220c6bd5c3eb4bdf872b43f446c2affae9e8d76ad4cfa8aae","status":9},
	{"dateCreated":1582349499000,"blockNo":55,"fee":"1260000000000000000","fromAddress":"FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9","toAddress":"FW52C07C5DF30A810D89866308E7CA973C1974DCD92F","value":"0","hash":"fx860432eda2bf09abd3d8466d8b8d5c500dcc17788e40ae0c077bf0ea948fa9d2","status":9},
	{"dateCreated":1582349489000,"blockNo":53,"fee":"1260000000000000000","fromAddress":"FWC58026E5D9C64D4F427E48C8A341AF50732DEB46A9","toAddress":"FW52C07C5DF30A810D89866308E7CA973C1974DCD92F","value":"6000000000000000000","hash":"fxb36661eba0e8c0133cee8c4a41bfe4b419636b081a1844ba0a19f885f0e1540a","status":9}
	]
}
err为请求结果，0表示请求成功
data为交易列表，其中：
	dateCreated为交易时间
	blockNo为所在区块号
	fee为该交易手续费
	fromAddress为交易来源地址
	toAddress为交易目标地址
	value为交易金额
	hash为交易哈希
	status为交易状态，9为成功、-2为无此交易、-1为失败、0为pending


查询某地址的最新50条交易：
GET请求：/faaGetAddressTransactions?address=FW7E4ED68DD482273040E54E2EF8927A1F0705C47CCB
参数address为钱包地址

结果：
{"err":0,
"data":
	[
	{"dateCreated":1593747011000,"blockNo":845401,"fee":"705000000000000000","fromAddress":"FWEC8B962E00C9466240B1075FE6EECAD97A8B64EB28","toAddress":"FW1DD13EE98E5DF3F1F193B5C12781469A04BAC77AB2","value":"565375000000000000000","hash":"fx538b95c5f8d748623ac2849a51ecf50296214aa21bdb5da95c336e8c118b0a37","hexdata":"0xf88595ec8b962e00c9466240b1075fe6eecad97a8b64eb28951dd13ee98e5df3f1f193b5c12781469a04bac77ab28b027b64f993e5ccb17980008809c8a9c3c6a68000801ba098ab8a8f635e4e39be7aa38ec27632309045764917d26aa0c20013203fa2fa45a0171432dc6632ef23503b621eef51d4bc399735df286111ea2e91d5c125a8276a","status":9},
	{"dateCreated":1593746313000,"blockNo":845331,"fee":"2000000000000000000","fromAddress":"FW5E83296C47665182E1C31CE2815798857C8DA5D2AD","toAddress":"FWEC8B962E00C9466240B1075FE6EECAD97A8B64EB28","value":"566080000000000000000","hash":"fx9d16eda56ad8a5b50f3caf53cde133931d59cbf5b056f323e0843f0b87fd9192","hexdata":"0xf885955e83296c47665182e1c31ce2815798857c8da5d2ad95ec8b962e00c9466240b1075fe6eecad97a8b64eb288b027b65035c8f9078200000881bc16d674ec80000801ca0a5b133220b5faa0442187b907b132abe4ac5da18d575b45a0ab726e6d566337ea016c4d3653352069837e50f5bc2255ec95811a086729b46f8404fc46540c8d5ad","status":9},
	{"dateCreated":1593655237000,"blockNo":836228,"fee":"1425000000000000000","fromAddress":"FWEC8B962E00C9466240B1075FE6EECAD97A8B64EB28","toAddress":"FW1DD13EE98E5DF3F1F193B5C12781469A04BAC77AB2","value":"879855000000000000000","hash":"fxb957aab36bf223cc663944581600c0ad02f8103414b055c644156239d366add8","hexdata":"0xf88595ec8b962e00c9466240b1075fe6eecad97a8b64eb28951dd13ee98e5df3f1f193b5c12781469a04bac77ab28b027b7605dd4b9cf65180008813c69df334ee8000801ba025fdbf455502bc609f87a731a47e15694282fba2881e6f08287e23dc5a9f9bfba06ae0544bea3a999ef4d4210046781f359f0e4729ca9bdd28478a8fd0ed8b9437","status":9},
	{"dateCreated":1593655024000,"blockNo":836208,"fee":"2000000000000000000","fromAddress":"FW5E83296C47665182E1C31CE2815798857C8DA5D2AD","toAddress":"FWEC8B962E00C9466240B1075FE6EECAD97A8B64EB28","value":"881280000000000000000","hash":"fx84cb53d0a39d5b5e63917d4436ef51ce592d5f50e1ad157db6db8335c2d976a2","hexdata":"0xf885955e83296c47665182e1c31ce2815798857c8da5d2ad95ec8b962e00c9466240b1075fe6eecad97a8b64eb288b027b7619a3e9902b400000881bc16d674ec80000801ca030b233d0703489451350c044f89cc9b260827909bbe995ed0504d9a40c500496a032228bcfc1e54201fabb65c9b93ef01103bf5f0afe9d19cecc601ccd08e7be2b","status":9}
	]
}
err为请求结果，0表示请求成功
data为交易列表，其中：
	dateCreated为交易时间
	blockNo为所在区块号
	fee为该交易手续费
	fromAddress为交易来源地址
	toAddress为交易目标地址
	value为交易金额
	hash为交易哈希
	status为交易状态，9为成功、-2为无此交易、-1为失败、0为pending