<?php

$data = array();

$handle = @fopen("strings.txt", "r");
if ($handle) {
    while (($buffer = fgets($handle, 4096)) !== false) {
        $data[] = $buffer;
    }
    if (!feof($handle)) {
        echo "Error: unexpected fgets() fail\n";
    }
    fclose($handle);
}

$outdata = array();

foreach($data as $line){
	$line=trim($line);
	$line.=chr(0);
	if(strlen($line)%2==1) $line.=chr(0);
	for($i=0; $i<strlen($line);$i++){
		$char = substr($line, $i, 1);
		$int = ord($char);
		$bin = decbin($int);
		$bin7 = str_pad($bin,7,"0",STR_PAD_LEFT);
		$outdata[] = compact('char','int','bin','bin7');
	}
}

if(count($outdata)%2==1) $outdata[] =array('char'=>'0', 'int'=>0, 'bin'=>'0000000', 'bin7'=>'0000000');

$final = array();

$buffer=array();

$finalC = 0;

do {
	if(count($buffer)==2){
		$encoded = str_pad($buffer[0]['bin7'] . $buffer[1]['bin7'], 20, "0", STR_PAD_LEFT);  // encode 2 chars to 20 bytes, little endian		
		$line = $encoded .'    '. str_pad(strval($finalC), 4, "0", STR_PAD_LEFT) .': '; 
		$line .= ($buffer[0]['int']>20 ? $buffer[0]['char'] : ' ');
		$line .= ($buffer[1]['int']>20 ? $buffer[1]['char'] : ' ') .' ';
		$line .= '\\' . $buffer[0]['int'] . '\\' . $buffer[1]['int'];
		$final[$finalC++] = $line.PHP_EOL;
		$buffer=array();
	}

	$buffer[] = array_shift($outdata);

} while(count($outdata)>0);
	
	if(count($buffer)==2){
		$encoded = str_pad($buffer[0]['bin7'] . $buffer[1]['bin7'], 20, "0", STR_PAD_LEFT);  // encode 2 chars to 20 bytes, little endian		
		$line = $encoded .'    '. str_pad(strval($finalC), 4, "0", STR_PAD_LEFT) .': '; 
		$line .= ($buffer[0]['int']>20 ? $buffer[0]['char'] : ' ');
		$line .= ($buffer[1]['int']>20 ? $buffer[1]['char'] : ' ') .' ';
		$line .= '\\' . $buffer[0]['int'] . '\\' . $buffer[1]['int'];
		$final[$finalC++] = $line.PHP_EOL;
		$buffer=array();
	}

file_put_contents('output.txt', $final);
