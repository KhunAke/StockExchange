create table public.bualuang_board_daily (symbol varchar(16) not null, date date not null, open float8, high float8, low float8, close float8, volume int8, value float8, primary key (symbol, date), unique (symbol, date));
create table public.set_company (symbol varchar(16) not null unique, name varchar(255), market varchar(8), industry varchar(255), sector varchar(255), update date, primary key (symbol), unique (symbol));
create table public.settrade_board (symbol varchar(16) not null, date timestamp not null, open float8, high float8, low float8, last float8, bid float8, bid_volume int8, offer float8, offer_volume int8, volume int8, value float8, primary key (symbol, date), unique (symbol, date));
create table public.settrade_index (symbol varchar(16) not null, date timestamp not null, last float8, high float8, low float8, volume int8, value float8, primary key (symbol, date), unique (symbol, date));
create table public.streaming_bids_offers (symbol varchar(16) not null, date timestamp not null, price float8 not null, bid_volume int8, offer_volume int8, primary key (symbol, date, price), unique (symbol, date, price));
create table public.streaming_order (order_no int8, symbol varchar(16) not null, date timestamp not null, side varchar(1), price float8, volume int4, match int4, balance int4, cancelled int4, status varchar(32), primary key (symbol, date), unique (symbol, date));
create table public.streaming_ticker (date date not null, type int2, market int2 not null, n int2, time timestamp, side varchar(1), price float8, close float8, change float8, change_percent float8, sequence int4 not null, a varchar(2), b varchar(2), volume int4, symbol varchar(16), primary key (date, market, sequence), unique (date, market, sequence));
